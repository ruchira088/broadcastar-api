package services.storage.store

import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait FileStore {
  type FullPath = String

  def read(key: String)(implicit executionContext: ExecutionContext): OptionT[Future, Array[Byte]]

  def write(key: String, data: Array[Byte])(implicit executionContext: ExecutionContext): Future[FullPath]
}
