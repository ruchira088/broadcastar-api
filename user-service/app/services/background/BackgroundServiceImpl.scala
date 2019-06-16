package services.background

import akka.Done
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.ruchij.shared.kafka.KafkaMessage
import com.ruchij.shared.kafka.producer.KafkaProducer
import dao.user.models.DatabaseUser
import javax.inject.{Inject, Singleton}
import services.triggering.TriggeringService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackgroundServiceImpl @Inject()(triggeringService: TriggeringService, kafkaProducer: KafkaProducer)(
  implicit materializer: Materializer
) extends BackgroundService {

  override def sendNewUsersToKafka()(implicit executionContext: ExecutionContext): Future[Done] =
    triggeringService
      .userCreated()
      .mapAsync(1) { databaseUser =>
        kafkaProducer
          .publish {
            KafkaMessage(DatabaseUser.toUser(databaseUser))
          }
          .flatMap { recordMetadata =>
            triggeringService.commitUserCreated(databaseUser)
          }
      }
      .runWith(Sink.ignore)
}
