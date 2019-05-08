package modules

import java.nio.file.Paths

import com.google.inject.{AbstractModule, Provides, Singleton}
import config.{LocalFileStoreConfiguration, S3Configuration}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import ec.{BlockingExecutionContext, BlockingExecutionContextImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.storage.store.{FileStore, LocalFileStore, S3FileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.user.{UserService, UserServiceImpl}
import software.amazon.awssdk.services.s3.S3AsyncClient
import utils.SystemUtilities

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

class CoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[CryptographyService]).to(classOf[BCryptService])
    bind(classOf[BlockingExecutionContext]).to(classOf[BlockingExecutionContextImpl])
    bind(classOf[StorageService]).to(classOf[StorageServiceImpl])
    bind(classOf[LocalFileStoreConfiguration]).toInstance(LocalFileStoreConfiguration(Paths.get("./file-storage")))
    bind(classOf[S3Configuration]).toInstance(S3Configuration("broadcastar-api-resources"))
    bind(classOf[S3AsyncClient]).toInstance(S3AsyncClient.create())
    bind(classOf[FileStore]).to(classOf[LocalFileStore])
  }

  @Singleton
  @Provides
  def databaseUserDao(
    slickDatabaseUserDao: SlickDatabaseUserDao
  )(implicit executionContext: ExecutionContext): DatabaseUserDao =
    Await.result(slickDatabaseUserDao.initialize().map(createdTable => slickDatabaseUserDao), 60 seconds)

  @Singleton
  @Provides
  def resourceInformationDao(
    slickResourceInformationDao: SlickResourceInformationDao
  )(implicit executionContext: ExecutionContext): ResourceInformationDao =
    Await.result(slickResourceInformationDao.initialize().map(_ => slickResourceInformationDao), 60 seconds)
}
