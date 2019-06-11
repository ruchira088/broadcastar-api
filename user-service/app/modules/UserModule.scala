package modules

import com.google.inject.{AbstractModule, Provides, Singleton, TypeLiteral}
import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.requests.SessionTokenExtractor
import com.typesafe.config.ConfigFactory
import config.{ApplicationConfiguration, SessionConfiguration, LocalFileStoreConfiguration, S3Configuration}
import dao.authentication.{AuthenticationTokenDao, SlickAuthenticationTokenDao}
import dao.reset.{ResetPasswordTokenDao, SlickResetPasswordTokenDao}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import dao.verification.{EmailVerificationTokenDao, SlickEmailVerificationTokenDao}
import ec.{BlockingExecutionContext, BlockingExecutionContextImpl}
import modules.UserModule.await
import play.api.libs.json.Json
import services.authentication.{AuthenticationService, AuthenticationServiceImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.notification.NotificationService
import services.notification.console.ConsoleNotificationService
import services.notification.models.NotificationType
import services.storage.store.{FileStore, LocalFileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.user.{UserService, UserServiceImpl}
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class UserModule extends AbstractModule {

  override def configure(): Unit = {
    val applicationConfiguration = ApplicationConfiguration.parse(ConfigFactory.load()).get

    println {
      Json.prettyPrint {
        Json.toJson(applicationConfiguration)
      }
    }

    bind(classOf[BuildInformation]).toInstance(applicationConfiguration.applicationInformation)
    bind(classOf[SessionConfiguration]).toInstance(applicationConfiguration.authenticationConfiguration)
    bind(classOf[LocalFileStoreConfiguration]).toInstance(applicationConfiguration.localFileStoreConfiguration)
    bind(classOf[S3Configuration]).toInstance(applicationConfiguration.s3Configuration)

    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[CryptographyService]).to(classOf[BCryptService])
    bind(classOf[BlockingExecutionContext]).to(classOf[BlockingExecutionContextImpl])
    bind(classOf[StorageService]).to(classOf[StorageServiceImpl])
    bind(classOf[AuthenticationService]).to(classOf[AuthenticationServiceImpl])
    bind(classOf[FileStore]).to(classOf[LocalFileStore])

    bind(new TypeLiteral[SessionTokenExtractor[String]] {}).toInstance(SessionTokenExtractor)
    bind(new TypeLiteral[NotificationService[NotificationType.Console.type]] {}).to(classOf[ConsoleNotificationService])
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
  def emailVerificationTokenDao(slickEmailVerificationTokenDao: SlickEmailVerificationTokenDao)(implicit executionContext: ExecutionContext): EmailVerificationTokenDao =
    await(slickEmailVerificationTokenDao.initialize().map(_ => slickEmailVerificationTokenDao))

  @Singleton
  @Provides
  def passwordResetToken(slickResetPasswordTokenDao: SlickResetPasswordTokenDao)(implicit executionContext: ExecutionContext): ResetPasswordTokenDao =
    await(slickResetPasswordTokenDao.initialize().map(_ => slickResetPasswordTokenDao))

  @Singleton
  @Provides
  def s3AsyncClient(): S3AsyncClient = S3AsyncClient.create()
}

object UserModule {
  private def await[A](future: Future[A]): A = Await.result(future, 60 seconds)
}
