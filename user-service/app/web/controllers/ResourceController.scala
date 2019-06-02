package web.controllers

import akka.util.ByteString
import com.ruchij.shared.web.responses.models.AggregatedResultsResponse
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.libs.Files
import play.api.mvc._
import services.storage.StorageService
import web.responses.ResponseCreator
import web.responses.models.FileUploadResult

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ResourceController @Inject()(storageService: StorageService, controllerComponents: ControllerComponents)(
  implicit executionContext: ExecutionContext
) extends AbstractController(controllerComponents) {

  def upload(): Action[MultipartFormData[Files.TemporaryFile]] =
    Action.async(parse.multipartFormData) { request =>
      ResponseCreator.create(Created) {
        Future
          .sequence(request.body.files.map { file =>
            storageService
              .upload(file.filename, file.ref.path, file.contentType, file.fileSize)
              .map { fileKey =>
                FileUploadResult(file.key, fileKey, file.filename, file.fileSize, file.contentType)
              }
          })
          .map(results => AggregatedResultsResponse(results.toList))
      }
    }

  def fetch(key: String): Action[AnyContent] =
    Action.async {
      storageService.fetch(key)
        .map { fileData =>
          Result(
            header = ResponseHeader(OK, Map.empty),
            body = HttpEntity.Strict(ByteString(fileData.data), fileData.resourceInformation.contentType)
          )
        }
    }
}
