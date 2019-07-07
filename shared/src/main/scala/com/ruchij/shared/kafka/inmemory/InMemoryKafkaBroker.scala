package com.ruchij.shared.kafka.inmemory

import java.util.concurrent.{CompletableFuture, CompletionStage}

import akka.actor.ActorRef
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.GroupTopicPartition
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.{Done, NotUsed}
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.consumer.KafkaConsumer
import com.ruchij.shared.kafka.producer.KafkaProducer
import com.ruchij.shared.kafka.{KafkaMessage, KafkaTopic}
import com.ruchij.shared.utils.SystemUtilities
import com.sksamuel.avro4s.Record
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Random, Try}

@Singleton
class InMemoryKafkaBroker @Inject()(kafkaConfiguration: KafkaConfiguration)(
  implicit materializer: Materializer,
  systemUtilities: SystemUtilities
) extends KafkaProducer
    with KafkaConsumer {

  val (actorRef, source): (ActorRef, Source[(KafkaTopic[_], Record), NotUsed]) =
    Source
      .actorRef[(KafkaTopic[_], Record)](Short.MaxValue.toInt, OverflowStrategy.fail)
      .toMat(BroadcastHub.sink)(Keep.both)
      .run()

  override def publish[A](
    message: KafkaMessage[A]
  )(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    actorRef ! message.kafkaTopic.recordFormat.to(message.value)

    println(message.value)

    Future.successful {
      new RecordMetadata(
        new TopicPartition(
          message.kafkaTopic.name(kafkaConfiguration),
          Random.nextInt(kafkaConfiguration.topicPartitionCount)
        ),
        0,
        0,
        systemUtilities.currentTime().getMillis,
        0L,
        0,
        0
      )
    }
  }

  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, ConsumerMessage.CommittableOffset), _] =
    source
      .collect {
        case (topic, record: Record) if topic.key == kafkaTopic.key => record
      }
      .mapAsync(1) { record =>
        Future.fromTry {
          Try(kafkaTopic.recordFormat.from(record))
        }
      }
      .map {
        _ -> new ConsumerMessage.CommittableOffset {
          override def partitionOffset: ConsumerMessage.PartitionOffset =
            ConsumerMessage.PartitionOffset(
              GroupTopicPartition(
                kafkaConfiguration.consumerGroupId.getOrElse(systemUtilities.randomUuid().toString),
                kafkaTopic.name(kafkaConfiguration),
                Random.nextInt(kafkaConfiguration.topicPartitionCount)
              ),
              0
            )

          override def commitScaladsl(): Future[Done] =
            Future.successful(Done)

          override def commitJavadsl(): CompletionStage[Done] =
            CompletableFuture.completedFuture(Done)

          override def batchSize: Long = 100
        }
      }

}
