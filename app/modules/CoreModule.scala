package modules

import java.nio.file.Paths

import com.google.inject.{AbstractModule, Provides, Singleton}
import config.{AuthenticationConfiguration, LocalFileStoreConfiguration, S3Configuration}
import dao.authentication.{AuthenticationTokenDao, SlickAuthenticationTokenDao}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import dao.verification.{EmailVerificationEntryDao, SlickEmailVerificationEntryDao}
import ec.{BlockingExecutionContext, BlockingExecutionContextImpl}
import modules.CoreModule.await
import services.authentication.{AuthenticationService, AuthenticationServiceImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.storage.store.{FileStore, LocalFileStore, S3FileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.user.{UserService, UserServiceImpl}
import software.amazon.awssdk.services.s3.S3AsyncClient
import utils.SystemUtilities

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class CoreModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[CryptographyService]).to(classOf[BCryptService])
    bind(classOf[BlockingExecutionContext]).to(classOf[BlockingExecutionContextImpl])
    bind(classOf[StorageService]).to(classOf[StorageServiceImpl])
    bind(classOf[LocalFileStoreConfiguration]).toInstance(LocalFileStoreConfiguration(Paths.get("./file-storage")))
    bind(classOf[S3Configuration]).toInstance(S3Configuration("chirper-api-resources"))
    bind(classOf[S3AsyncClient]).toInstance(S3AsyncClient.create())
    bind(classOf[AuthenticationConfiguration]).toInstance(AuthenticationConfiguration(10 minutes))
    bind(classOf[AuthenticationService]).to(classOf[AuthenticationServiceImpl])
    bind(classOf[FileStore]).to(classOf[LocalFileStore])
  }

  @Singleton
  @Provides
  def databaseUserDao(
    slickDatabaseUserDao: SlickDatabaseUserDao
  )(implicit executionContext: ExecutionContext): DatabaseUserDao =
    await(slickDatabaseUserDao.initialize().map(createdTable => slickDatabaseUserDao))

  @Singleton
  @Provides
  def resourceInformationDao(
    slickResourceInformationDao: SlickResourceInformationDao
  )(implicit executionContext: ExecutionContext): ResourceInformationDao =
    await(slickResourceInformationDao.initialize().map(_ => slickResourceInformationDao))

  @Singleton
  @Provides
  def authenticationTokenDao(
    slickAuthenticationTokenDao: SlickAuthenticationTokenDao
  )(implicit executionContext: ExecutionContext): AuthenticationTokenDao =
    await(slickAuthenticationTokenDao.initialize().map(_ => slickAuthenticationTokenDao))

  @Singleton
  @Provides
  def emailVerificationEntryDao(slickEmailVerificationEntryDao: SlickEmailVerificationEntryDao)(implicit executionContext: ExecutionContext): EmailVerificationEntryDao =
    await(slickEmailVerificationEntryDao.initialize().map(_ => slickEmailVerificationEntryDao))
}

object CoreModule {
  private def await[A](future: Future[A]): A = Await.result(future, 60 seconds)
}
