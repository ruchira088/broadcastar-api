package services.storage

import java.io.FileNotFoundException
import java.nio.file.Path

import com.ruchij.shared.utils.{IOUtils, SystemUtilities}
import com.ruchij.shared.utils.MonadicUtils.{OptionTWrapper, withDefault}
import dao.resource.ResourceInformationDao
import exceptions.FatalWebServerException
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.storage.models.{FileData, ResourceInformation}
import services.storage.store.FileStore

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageServiceImpl @Inject()(fileStore: FileStore, resourceInformationDao: ResourceInformationDao)(
  implicit systemUtilities: SystemUtilities
) extends StorageService {

  override def upload(fileName: String, filePath: Path, contentType: Option[String], fileSize: Long)(
    implicit executionContext: ExecutionContext
  ): Future[FileKey] =
    for {
      data <- IOUtils.readFile(filePath) ifEmpty Future.failed(FatalWebServerException)

      fileKey = s"${systemUtilities.randomUuid()}-$fileName"

      _ <- Future.sequence {
        List(
          fileStore.write(fileKey, data),
          resourceInformationDao.insert {
            ResourceInformation(fileKey, systemUtilities.currentTime(), fileName, contentType, fileSize)
          }
        )
      }
    } yield fileKey

  override def fetch(key: FileKey)(implicit executionContext: ExecutionContext): Future[FileData] =
    withDefault(Future.failed(new FileNotFoundException(s"Resource key not found: $key"))) {
      for {
        resourceInformation <- resourceInformationDao.getByKey(key)
        fileData <- fileStore.read(key)
      }
      yield FileData(resourceInformation, fileData)
    }
}
