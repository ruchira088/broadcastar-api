package modules

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides, Singleton, TypeLiteral}
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.kafka.producer.{KafkaProducer, KafkaProducerImpl}
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.requests.SessionTokenExtractor
import com.typesafe.config.ConfigFactory
import config.{ApplicationConfiguration, LocalFileStoreConfiguration, S3Configuration, SessionConfiguration, TriggerConfiguration}
import dao.InitializableTable
import dao.authentication.{AuthenticationTokenDao, SlickAuthenticationTokenDao}
import dao.offset.{OffsetDao, SlickOffsetDao}
import dao.reset.{ResetPasswordTokenDao, SlickResetPasswordTokenDao}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import dao.verification.{EmailVerificationTokenDao, SlickEmailVerificationTokenDao}
import ec.{BlockingExecutionContext, BlockingExecutionContextImpl}
import modules.UserModule.{await, initialize}
import play.api.libs.json.Json
import services.authentication.{AuthenticationService, AuthenticationServiceImpl}
import services.background.{BackgroundService, BackgroundServiceImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.notification.NotificationService
import services.notification.console.ConsoleNotificationService
import services.notification.models.NotificationType
import services.storage.store.{FileStore, LocalFileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.triggering.{TriggeringService, TriggeringServiceImpl}
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
    bind(classOf[TriggerConfiguration]).toInstance(applicationConfiguration.triggerConfiguration)
    bind(classOf[KafkaConfiguration]).toInstance(applicationConfiguration.kafkaConfiguration)

    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[CryptographyService]).to(classOf[BCryptService])
    bind(classOf[BlockingExecutionContext]).to(classOf[BlockingExecutionContextImpl])
    bind(classOf[StorageService]).to(classOf[StorageServiceImpl])
    bind(classOf[AuthenticationService]).to(classOf[AuthenticationServiceImpl])
    bind(classOf[FileStore]).to(classOf[LocalFileStore])
    bind(classOf[TriggeringService]).to(classOf[TriggeringServiceImpl])

    bind(new TypeLiteral[SessionTokenExtractor[String]] {}).toInstance(SessionTokenExtractor)
    bind(new TypeLiteral[NotificationService[NotificationType.Console.type]] {}).to(classOf[ConsoleNotificationService])
  }

  @Singleton
  @Provides
  def databaseUserDao(
    slickDatabaseUserDao: SlickDatabaseUserDao
  )(implicit executionContext: ExecutionContext): DatabaseUserDao =
    await(initialize(slickDatabaseUserDao))

  @Singleton
  @Provides
  def resourceInformationDao(
    slickResourceInformationDao: SlickResourceInformationDao
  )(implicit executionContext: ExecutionContext): ResourceInformationDao =
    await(initialize(slickResourceInformationDao))

  @Singleton
  @Provides
  def authenticationTokenDao(
    slickAuthenticationTokenDao: SlickAuthenticationTokenDao
  )(implicit executionContext: ExecutionContext): AuthenticationTokenDao =
    await(initialize(slickAuthenticationTokenDao))

  @Singleton
  @Provides
  def emailVerificationTokenDao(slickEmailVerificationTokenDao: SlickEmailVerificationTokenDao)(implicit executionContext: ExecutionContext): EmailVerificationTokenDao =
    await(initialize(slickEmailVerificationTokenDao))

  @Singleton
  @Provides
  def passwordResetTokenDao(slickResetPasswordTokenDao: SlickResetPasswordTokenDao)(implicit executionContext: ExecutionContext): ResetPasswordTokenDao =
    await(initialize(slickResetPasswordTokenDao))

  @Singleton
  @Provides
  def offsetDao(slickOffsetDao: SlickOffsetDao)(implicit executionContext: ExecutionContext): OffsetDao =
    await(initialize(slickOffsetDao))

  @Singleton
  @Provides
  def s3AsyncClient(): S3AsyncClient = S3AsyncClient.create()

  @Singleton
  @Provides
  def backgroundService(backgroundServiceImpl: BackgroundServiceImpl)(implicit executionContext: ExecutionContext): BackgroundService = {
    backgroundServiceImpl.sendNewUsersToKafka()
      .recoverWith {
        case throwable =>
          println(throwable)
          Future.failed(throwable)
      }

    println("Starting background service...")

    backgroundServiceImpl
  }

  @Singleton
  @Provides
  def kafkaProducer(kafkaConfiguration: KafkaConfiguration)(implicit actorSystem: ActorSystem): KafkaProducer =
    new KafkaProducerImpl(KafkaProducerImpl.settings(kafkaConfiguration))
}

object UserModule {
  private def await[A](future: Future[A]): A = Await.result(future, 60 seconds)

  private def initialize[A <: InitializableTable](table: A)(implicit executionContext: ExecutionContext): Future[A] =
    table.initialize().map(_ => table)
}
