package modules

import com.google.inject.{AbstractModule, Provides, Singleton, TypeLiteral}
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.kafka.inmemory.InMemoryKafkaBroker
import com.ruchij.shared.kafka.producer.{KafkaProducer, KafkaProducerImpl}
import com.ruchij.shared.monads.MonadicUtils
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.requests.SessionTokenExtractor
import com.typesafe.config.ConfigFactory
import config._
import dao.authentication.{AuthenticationTokenDao, SlickAuthenticationTokenDao}
import dao.offset.{OffsetDao, SlickOffsetDao}
import dao.reset.{ResetPasswordTokenDao, SlickResetPasswordTokenDao}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import dao.verification.{EmailVerificationTokenDao, SlickEmailVerificationTokenDao}
import ec.{CpuIntensiveExecutionContext, CpuIntensiveExecutionContextImpl}
import play.api.libs.json.Json
import services.authentication.{AuthenticationService, AuthenticationServiceImpl}
import services.background.{BackgroundService, BackgroundServiceImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.storage.store.{FileStore, LocalFileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.triggering.{TriggeringService, TriggeringServiceImpl}
import services.user.{UserService, UserServiceImpl}
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class UserModule extends AbstractModule {

  override def configure(): Unit = {
    val applicationConfiguration =
      MonadicUtils.unsafe {
        ApplicationConfiguration.parse(ConfigFactory.load())
      }

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
    bind(classOf[CpuIntensiveExecutionContext]).to(classOf[CpuIntensiveExecutionContextImpl])
    bind(classOf[StorageService]).to(classOf[StorageServiceImpl])
    bind(classOf[AuthenticationService]).to(classOf[AuthenticationServiceImpl])
    bind(classOf[FileStore]).to(classOf[LocalFileStore])
    bind(classOf[TriggeringService]).to(classOf[TriggeringServiceImpl])
//    bind(classOf[KafkaProducer]).to(classOf[InMemoryKafkaBroker])
    bind(classOf[KafkaProducer]).to(classOf[KafkaProducerImpl])

    bind(classOf[DatabaseUserDao]).to(classOf[SlickDatabaseUserDao])
    bind(classOf[ResourceInformationDao]).to(classOf[SlickResourceInformationDao])
    bind(classOf[AuthenticationTokenDao]).to(classOf[SlickAuthenticationTokenDao])
    bind(classOf[EmailVerificationTokenDao]).to(classOf[SlickEmailVerificationTokenDao])
    bind(classOf[ResetPasswordTokenDao]).to(classOf[SlickResetPasswordTokenDao])
    bind(classOf[OffsetDao]).to(classOf[SlickOffsetDao])

    bind(new TypeLiteral[SessionTokenExtractor[String]] {}).toInstance(SessionTokenExtractor)
  }

  @Singleton
  @Provides
  def s3AsyncClient(): S3AsyncClient = S3AsyncClient.create()

  @Singleton
  @Provides
  def backgroundService(backgroundServiceImpl: BackgroundServiceImpl)(implicit executionContext: ExecutionContext): BackgroundService = {
    backgroundServiceImpl.start()
      .recoverWith {
        case throwable =>
          println(throwable)
          throwable.printStackTrace()

          Future.successful(backgroundService(backgroundServiceImpl))
      }

    println("Starting background service...")

    backgroundServiceImpl
  }
}
