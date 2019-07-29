package modules

import com.ruchij.shared.config.DevelopmentConfiguration
import com.ruchij.shared.config.models.DevelopmentMode
import com.ruchij.shared.exceptions.aggregation.AggregatedConfigurationException
import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.kafka.config.{KafkaClientConfiguration, KafkaTopicConfiguration}
import com.ruchij.shared.kafka.inmemory.InMemoryKafkaBroker
import com.ruchij.shared.kafka.producer.{KafkaProducer, KafkaProducerImpl}
import com.ruchij.shared.monads.MonadicUtils.{sequence, sequenceFailFast, tryMonadError, unsafe}
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.requests.SessionTokenExtractor
import com.typesafe.config.Config
import config.{LocalFileStoreConfiguration, S3Configuration, SessionConfiguration, TriggerConfiguration}
import dao.authentication.{AuthenticationTokenDao, SlickAuthenticationTokenDao}
import dao.offset.{OffsetDao, SlickOffsetDao}
import dao.reset.{ResetPasswordTokenDao, SlickResetPasswordTokenDao}
import dao.resource.{ResourceInformationDao, SlickResourceInformationDao}
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import dao.verification.{EmailVerificationTokenDao, SlickEmailVerificationTokenDao}
import ec.{CpuIntensiveExecutionContext, CpuIntensiveExecutionContextImpl}
import play.api.inject.{Binding, bind}
import scalaz.ReaderT
import services.authentication.{AuthenticationService, AuthenticationServiceImpl}
import services.background.{BackgroundService, BackgroundServiceImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.storage.store.{FileStore, LocalFileStore, S3FileStore}
import services.storage.{StorageService, StorageServiceImpl}
import services.triggering.{TriggeringService, TriggeringServiceImpl}
import services.user.{UserService, UserServiceImpl}
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.language.reflectiveCalls
import scala.util.{Failure, Success, Try}

object UserServiceModule {
  type Bindings = List[Binding[_]]

  type UnsafeBindings = ReaderT[Try, Config, Either[List[Throwable], Bindings]]

  def unsafeBindingsRun(config: Config)(bindings: UnsafeBindings*): Bindings =
    unsafe {
      sequenceFailFast(
        bindings.map {
          _(config).flatMap {
            _.fold(errors => Failure(AggregatedConfigurationException(errors)), Success.apply)
          }
        }: _*
      )
        .map(_.flatten)
    }

  val developmentConfiguration: ReaderT[Try, Config, DevelopmentConfiguration] =
    ReaderT { DevelopmentConfiguration.parse }

  val coreConfigurationBindings: UnsafeBindings =
    ReaderT {
      config =>
        sequence(
          BuildInformation.parse(config).map(bind[BuildInformation].toInstance),
          SessionConfiguration.parse(config).map(bind[SessionConfiguration].toInstance),
          TriggerConfiguration.parse(config).map(bind[TriggerConfiguration].toInstance)
        )
    }

  val daoBindings: Bindings =
    List(
      bind[DatabaseUserDao].to[SlickDatabaseUserDao],
      bind[ResourceInformationDao].to[SlickResourceInformationDao],
      bind[AuthenticationTokenDao].to[SlickAuthenticationTokenDao],
      bind[EmailVerificationTokenDao].to[SlickEmailVerificationTokenDao],
      bind[ResetPasswordTokenDao].to[SlickResetPasswordTokenDao],
      bind[OffsetDao].to[SlickOffsetDao]
    )

  val serviceBindings: Bindings =
    List(
      bind[UserService].to[UserServiceImpl],
      bind[CryptographyService].to[BCryptService],
      bind[StorageService].to[StorageServiceImpl],
      bind[AuthenticationService].to[AuthenticationServiceImpl],
      bind[TriggeringService].to[TriggeringServiceImpl]
    )

  val backgroundBindings: Bindings =
    List(
      bind[BackgroundService].to[BackgroundServiceImpl],
      bind[OnStartup].toSelf.eagerly()
    )

  val localBindings: UnsafeBindings =
    ReaderT {
      config =>
        sequence(
          LocalFileStoreConfiguration.parse(config).map(bind[LocalFileStoreConfiguration].toInstance),
          Success { bind[FileStore].to[LocalFileStore] },
          Success { bind[KafkaProducer].to[InMemoryKafkaBroker] }
        )
    }

  val dockerComposeBindings: UnsafeBindings =
    ReaderT {
      config =>
        sequence(
          KafkaClientConfiguration.parseLocalConfig(config).map(bind[KafkaClientConfiguration].toInstance),
          KafkaTopicConfiguration.parse(config).map(bind[KafkaTopicConfiguration].toInstance),
          LocalFileStoreConfiguration.parse(config).map(bind[LocalFileStoreConfiguration].toInstance),
          Success { bind[FileStore].to[LocalFileStore] },
          Success { bind[KafkaProducer].to[KafkaProducerImpl] }
        )
    }

  val onlineBindings: UnsafeBindings =
    ReaderT {
      config =>
        sequence(
          S3Configuration.parse(config).map(bind[S3Configuration].toInstance),
          KafkaClientConfiguration.parseConfluentConfig(config).map(bind[KafkaClientConfiguration].toInstance),
          KafkaTopicConfiguration.parse(config).map(bind[KafkaTopicConfiguration].toInstance),
          Success { bind[FileStore].to[S3FileStore] },
          Success { bind[KafkaProducer].to[KafkaProducerImpl] },
          Success { bind[S3AsyncClient].toInstance(S3AsyncClient.create()) }
        )
    }

  val executionContextBindings: Bindings =
    List(bind[CpuIntensiveExecutionContext].to[CpuIntensiveExecutionContextImpl])

  val other: Bindings =
    List(
      bind[SessionTokenExtractor].toInstance(SessionTokenExtractor),
      bind[SystemUtilities].toInstance(SystemUtilities),
    )
}
