package web.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.Files
import play.api.mvc.{AbstractController, Action, ControllerComponents, MultipartFormData}
import scalaz.std.scalaFuture.futureInstance
import services.storage.StorageService
import web.responses.ResponseCreator
import web.responses.models.{AggregatedResultsResponse, FileUploadResult}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ResourceController @Inject()(storageService: StorageService, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def upload(): Action[MultipartFormData[Files.TemporaryFile]] =
    Action.async(parse.multipartFormData) {
      request =>
        ResponseCreator.create(Created) {
          Future.sequence(
            request.body.files.map {
              file =>
                storageService.upload(file.filename, file.ref.path)
                  .map { fileKey =>  FileUploadResult(file.key, fileKey, file.filename, file.fileSize, file.contentType) }
            }
          )
            .map(results => AggregatedResultsResponse(results.toList))
        }
    }
}
