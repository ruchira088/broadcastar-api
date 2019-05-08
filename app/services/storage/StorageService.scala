package services.storage

import java.nio.file.Path

import services.storage.models.FileData

import scala.concurrent.{ExecutionContext, Future}

trait StorageService {
  type FileKey = String

  def upload(fileName: String, filePath: Path, contentType: Option[String], fileSize: Long)(
    implicit executionContext: ExecutionContext
  ): Future[FileKey]

  def fetch(key: FileKey)(implicit executionContext: ExecutionContext): Future[FileData]
}
