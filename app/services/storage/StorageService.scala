package services.storage

import java.nio.file.Path

import scala.concurrent.{ExecutionContext, Future}

trait StorageService {
  type FileKey = String

  def upload(fileName: String, filePath: Path)(implicit executionContext: ExecutionContext): Future[FileKey]
}
