package com.ruchij.shared.kafka.stubs

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CompletableFuture, CompletionStage}

import akka.Done
import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.GroupTopicPartition
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.ruchij.shared.kafka.consumer.KafkaConsumer
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.kafka.producer.KafkaProducer
import com.ruchij.shared.kafka.stubs.models.StubMessage
import com.ruchij.shared.kafka.{KafkaMessage, KafkaTopic}
import com.ruchij.shared.models.EmailVerificationToken
import com.ruchij.shared.utils.{RandomGenerator, SystemUtilities}
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Try

class StubKafkaBroker(implicit materializer: Materializer, systemUtilities: SystemUtilities)
    extends KafkaProducer
    with KafkaConsumer {

  private val (actorRef, source): (ActorRef, Source[StubMessage[_], _]) =
    Source.actorRef[StubMessage[_]](Int.MaxValue, OverflowStrategy.fail).toMat(BroadcastHub.sink)(Keep.both).run()

  private val offset: AtomicInteger = new AtomicInteger(0)

  override def publish[A](
    kafkaMessage: KafkaMessage[A]
  )(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val recordMetadata =
      new RecordMetadata(
        new TopicPartition(kafkaMessage.kafkaTopic.key, 0),
        0,
        offset.incrementAndGet(),
        systemUtilities.currentTime().getMillis,
        kafkaMessage.value.hashCode().toLong,
        0,
        kafkaMessage.value.toString.length
      )

    actorRef ! StubMessage(
      kafkaMessage.kafkaTopic,
      kafkaMessage.kafkaTopic.recordFormat.to(kafkaMessage.value),
      recordMetadata
    )

    Future.successful(recordMetadata)
  }

  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, ConsumerMessage.CommittableOffset), _] =
    source
      .filter {
        _.kafkaTopic.key == kafkaTopic.key
      }
      .mapAsync(parallelism = 1) { stubMessage =>
        Future.fromTry {
          Try { kafkaTopic.recordFormat.from(stubMessage.genericRecord) }
            .map {
              _ -> new ConsumerMessage.CommittableOffset {
                override def partitionOffset: ConsumerMessage.PartitionOffset =
                  ConsumerMessage.PartitionOffset(
                    GroupTopicPartition(
                      "stub-kafka-consumer",
                      stubMessage.recordMetadata.topic(),
                      stubMessage.recordMetadata.partition()
                    ),
                    stubMessage.recordMetadata.offset()
                  )

                override def commitScaladsl(): Future[Done] = Future.successful(Done)

                override def commitJavadsl(): CompletionStage[Done] = CompletableFuture.completedFuture(Done)

                override def batchSize: Long = 1
              }
            }
        }
      }
}

object StubKafkaBroker {
  def publishGeneratedMessages[A](kafkaTopic: KafkaTopic[A], kafkaProducer: KafkaProducer, interval: FiniteDuration)(
    implicit randomGenerator: RandomGenerator[A],
    actorSystem: ActorSystem,
    executionContext: ExecutionContext
  ): Cancellable =
    actorSystem.scheduler.schedule(initialDelay = 1 second, interval) {
      kafkaProducer.publish(KafkaMessage(randomGenerator.generate())(kafkaTopic))
    }

  implicit def verificationEmailGenerator(
    implicit systemUtilities: SystemUtilities
  ): RandomGenerator[VerificationEmail] =
    for {
      user <- RandomGenerator.userGenerator
      index <- RandomGenerator.intGenerator(1000)
    }
    yield VerificationEmail(
      EmailVerificationToken(user.userId, systemUtilities.randomUuid(), index, user.email, systemUtilities.currentTime(), None),
      user
    )
}
