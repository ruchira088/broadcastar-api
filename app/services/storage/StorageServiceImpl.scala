package services.storage

import java.io.FileNotFoundException
import java.nio.file.Path

import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.storage.store.FileStore
import utils.MonadicUtils.OptionTWrapper
import utils.{IOUtils, SystemUtilities}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageServiceImpl @Inject()(fileStore: FileStore)(implicit systemUtilities: SystemUtilities)
    extends StorageService {

  override def upload(fileName: String, filePath: Path)(implicit executionContext: ExecutionContext): Future[FileKey] =
    for {
      data <- IOUtils.readFile(filePath) ifEmpty Future.failed(new FileNotFoundException(s"File not found at: ${filePath.toAbsolutePath}"))

      fileKey = s"${systemUtilities.randomUuid()}-$fileName"
      _ <- fileStore.write(fileKey, data)
    } yield fileKey
}
