package services.background

import akka.Done
import akka.actor.Cancellable
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink}
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.kafka.{KafkaMessage, KafkaTopic}
import com.ruchij.shared.kafka.producer.KafkaProducer
import dao.user.models.DatabaseUser
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.RecordMetadata
import services.triggering.TriggeringService
import services.user.UserService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackgroundServiceImpl @Inject()(triggeringService: TriggeringService, userService: UserService, kafkaProducer: KafkaProducer)(
  implicit materializer: Materializer
) extends BackgroundService {

  def publishNewUser(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    kafkaProducer.publish(KafkaMessage(DatabaseUser.toUser(databaseUser)))

  def verificationEmail(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[RecordMetadata] =
    userService.getEmailVerificationToken(databaseUser.userId)
      .flatMap {
        emailVerificationToken =>
          kafkaProducer.publish(KafkaMessage(VerificationEmail(emailVerificationToken, DatabaseUser.toUser(databaseUser))))
      }

  override def start()(implicit executionContext: ExecutionContext): Cancellable =
    triggeringService
      .userCreated()
      .mapAsync(parallelism = 1) { databaseUser =>
        Future.sequence {
          List(publishNewUser(databaseUser), verificationEmail(databaseUser))
        }
          .flatMap(_ => triggeringService.commitUserCreated(databaseUser))
      }
      .toMat(Sink.ignore)(Keep.left)
      .run()
}
