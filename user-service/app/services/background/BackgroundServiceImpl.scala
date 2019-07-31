package services.background

import akka.Done
import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import com.ruchij.shared.kafka.KafkaMessage
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.kafka.producer.KafkaProducer
import com.ruchij.shared.models.ResetPasswordToken
import com.typesafe.scalalogging.Logger
import dao.user.models.DatabaseUser
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.RecordMetadata
import services.triggering.TriggeringService
import services.user.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class BackgroundServiceImpl @Inject()(triggeringService: TriggeringService, userService: UserService, kafkaProducer: KafkaProducer)(
  implicit materializer: Materializer
) extends BackgroundService {
  private val logger = Logger[BackgroundServiceImpl]

  def publishNewUser(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    publish(KafkaMessage(DatabaseUser.toUser(databaseUser)), databaseUser.username)

  def verificationEmail(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    userService.getEmailVerificationToken(databaseUser.userId)
      .flatMap {
        emailVerificationToken =>
          publish(KafkaMessage(VerificationEmail(emailVerificationToken, DatabaseUser.toUser(databaseUser))), s"${databaseUser.username} <${databaseUser.email}>")
      }

  def forgotPassword(resetPasswordToken: ResetPasswordToken)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    publish(KafkaMessage(resetPasswordToken), resetPasswordToken.email)

  def publish[A](kafkaMessage: KafkaMessage[A], summary: String)(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    logger.info(s"Publishing to Kafka => ${kafkaMessage.kafkaTopic.key} $summary")

    kafkaProducer.publish(kafkaMessage)
      .andThen {
        case Success(_) => logger.info(s"Successfully published to Kafka => ${kafkaMessage.kafkaTopic.key} $summary")
        case Failure(throwable) =>
          logger.error(s"${throwable.getMessage} (${kafkaMessage.kafkaTopic.key}: $summary)", throwable)
      }
  }

  def userCreatedEvents()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
    triggeringService
      .userCreated()
      .mapAsync(parallelism = 1) { databaseUser =>
        logger.info(s"Received new user trigger: ${databaseUser.username}")

        Future.sequence {
          List(publishNewUser(databaseUser), verificationEmail(databaseUser))
        }
          .flatMap(_ => triggeringService.commitUserCreated(databaseUser))
      }
      .toMat(Sink.ignore)(Keep.both)
      .run()

  def forgotPasswordEvents()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
    triggeringService.forgotPassword()
      .mapAsync(parallelism = 1) {
        resetPasswordToken =>
          logger.info(s"Received forgot password event: ${resetPasswordToken.email}")

          forgotPassword(resetPasswordToken)
            .flatMap(_ => triggeringService.commitForgotPassword(resetPasswordToken))
      }
      .toMat(Sink.ignore)(Keep.both)
      .run()

  override def start()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
    BackgroundService.combine {
      List(
        userCreatedEvents(),
        forgotPasswordEvents()
      )
    }
}