package services.background

import akka.Done
import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import com.ruchij.shared.kafka.{KafkaMessage, KafkaTopic}
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.kafka.producer.KafkaProducer
import com.typesafe.scalalogging.Logger
import dao.user.models.DatabaseUser
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.RecordMetadata
import services.triggering.TriggeringService
import services.user.UserService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class BackgroundServiceImpl @Inject()(triggeringService: TriggeringService, userService: UserService, kafkaProducer: KafkaProducer)(
  implicit materializer: Materializer
) extends BackgroundService {
  private val logger = Logger[BackgroundServiceImpl]

  def publishNewUser(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    logger.info(s"Publishing to Kafka => UserCreated: ${databaseUser.username}")
    kafkaProducer.publish(KafkaMessage(DatabaseUser.toUser(databaseUser)))
      .andThen {
        case Success(_) => logger.info(s"Successfully published to Kafka => UserCreated: ${databaseUser.username}")
        case Failure(throwable) =>
          logger.error(s"${throwable.getMessage} (UserCreated: ${databaseUser.username})", throwable)
      }
  }

  def verificationEmail(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    userService.getEmailVerificationToken(databaseUser.userId)
      .flatMap {
        emailVerificationToken =>
          logger.info(s"Publishing to Kafka => EmailVerification: ${databaseUser.username} <${databaseUser.email}>")
          kafkaProducer.publish(KafkaMessage(VerificationEmail(emailVerificationToken, DatabaseUser.toUser(databaseUser))))
            .andThen {
              case Success(_) => logger.info(s"Successfully published to Kafka => EmailVerification: ${databaseUser.username} <${databaseUser.email}>")
              case Failure(throwable) =>
                logger.error(s"${throwable.getMessage} (EmailVerification: ${databaseUser.username} <${databaseUser.email}>)", throwable)
            }
      }

  override def start()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
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
}