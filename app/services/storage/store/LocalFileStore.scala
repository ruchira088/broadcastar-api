package services.storage.store

import config.LocalFileStoreConfiguration
import javax.inject.{Inject, Singleton}
import scalaz.OptionT
import utils.IOUtils

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LocalFileStore @Inject()(localFileStoreConfiguration: LocalFileStoreConfiguration) extends FileStore {
  override def read(key: String)(implicit executionContext: ExecutionContext): OptionT[Future, Array[Byte]] =
    IOUtils.readFile(localFileStoreConfiguration.storePath.resolve(key))

  override def write(key: String, data: Array[Byte])(implicit executionContext: ExecutionContext): Future[FullPath] =
    IOUtils.writeToFile(localFileStoreConfiguration.storePath.resolve(key), data)
      .map(_ => localFileStoreConfiguration.storePath.resolve(key).toAbsolutePath.toString)
}
