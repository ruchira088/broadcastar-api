package com.ruchij.shared.kafka.stubs

import java.util.concurrent.{CompletableFuture, CompletionStage}

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.GroupTopicPartition
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.consumer.KafkaConsumer
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.models.EmailVerificationToken
import com.ruchij.shared.utils.{RandomGenerator, SystemUtilities}
import org.apache.avro.generic.GenericRecord

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Random, Try}

class StubKafkaConsumer(implicit materializer: Materializer) extends KafkaConsumer {

  val (actorRef, source): (ActorRef, Source[GenericRecord, _]) =
    Source.actorRef[GenericRecord](Int.MaxValue, OverflowStrategy.fail).toMat(BroadcastHub.sink)(Keep.both).run()

  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, ConsumerMessage.CommittableOffset), _] =
    source
      .mapAsync(parallelism = 1) { genericRecord =>
        Future.fromTry {
          Try {
            kafkaTopic.recordFormat.from(genericRecord)
          }
        }
      }
      .map {
        _ -> new ConsumerMessage.CommittableOffset {
          override def partitionOffset: ConsumerMessage.PartitionOffset =
            ConsumerMessage.PartitionOffset(
              GroupTopicPartition("stub-kafka-consumer", kafkaTopic.key, 0),
              math.abs(Random.nextInt())
            )

          override def commitScaladsl(): Future[Done] = Future.successful(Done)

          override def commitJavadsl(): CompletionStage[Done] = CompletableFuture.completedFuture(Done)

          override def batchSize: Long = 1
        }
      }
}

object StubKafkaConsumer {
  def run[A](kafkaTopic: KafkaTopic[A], actorRef: ActorRef)(
    implicit randomGenerator: RandomGenerator[A],
    actorSystem: ActorSystem,
    executionContext: ExecutionContext
  ): Cancellable =
    actorSystem.scheduler.schedule(initialDelay = 2 seconds, interval = 5 seconds) {
      actorRef ! kafkaTopic.recordFormat.to(randomGenerator.generate())
    }

  implicit def verificationEmailGenerator(
    implicit systemUtilities: SystemUtilities
  ): RandomGenerator[VerificationEmail] =
    RandomGenerator.userGenerator.map { user =>
      VerificationEmail(
        EmailVerificationToken(
          user.userId,
          systemUtilities.randomUuid(),
          user.email,
          systemUtilities.currentTime(),
          None
        ),
        user
      )
    }

}
