package com.ruchij.shared.kafka.inmemory

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CompletableFuture, CompletionStage}

import akka.actor.ActorRef
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.GroupTopicPartition
import akka.stream.scaladsl.{BroadcastHub, Keep, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.{Done, NotUsed}
import com.ruchij.shared.kafka.consumer.KafkaConsumer
import com.ruchij.shared.kafka.inmemory.InMemoryKafkaBroker.InMemoryKafkaBrokerMessage
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
class InMemoryKafkaBroker @Inject()(
  implicit materializer: Materializer,
  systemUtilities: SystemUtilities
) extends KafkaProducer
    with KafkaConsumer {

  private val offset: AtomicLong = new AtomicLong(0)

  val (actorRef, source): (ActorRef, Source[InMemoryKafkaBrokerMessage, NotUsed]) =
    Source
      .actorRef[InMemoryKafkaBrokerMessage](Short.MaxValue.toInt, OverflowStrategy.fail)
      .toMat(BroadcastHub.sink)(Keep.both)
      .run()

  override def publish[A](
    message: KafkaMessage[A]
  )(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val recordMetadata =
      new RecordMetadata(
        new TopicPartition(
          message.kafkaTopic.name(InMemoryKafkaBroker.TOPIC_PREFIX),
          Random.nextInt(InMemoryKafkaBroker.PARTITION_COUNT)
        ),
        0,
        offset.getAndIncrement(),
        systemUtilities.currentTime().getMillis,
        message.hashCode().toLong,
        0,
        message.value.toString.length
      )

    actorRef ! InMemoryKafkaBrokerMessage(message.kafkaTopic.recordFormat.to(message.value), recordMetadata)

    println(message.value)

    Future.successful(recordMetadata)
  }

  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, ConsumerMessage.CommittableOffset), _] =
    source
      .filter {
        message =>  message.recordMetadata.topic == kafkaTopic.name(InMemoryKafkaBroker.TOPIC_PREFIX)
      }
      .mapAsync(1) { case InMemoryKafkaBrokerMessage(record, recordMetadata) =>
        Future.fromTry {
          Try(kafkaTopic.recordFormat.from(record))
            .map {
              _ -> new ConsumerMessage.CommittableOffset {
                override def partitionOffset: ConsumerMessage.PartitionOffset =
                  ConsumerMessage.PartitionOffset(
                    GroupTopicPartition(
                      InMemoryKafkaBroker.CONSUMER_GROUP_ID,
                      recordMetadata.topic(),
                      recordMetadata.partition()
                    ),
                    recordMetadata.offset()
                  )

                override def commitScaladsl(): Future[Done] = Future.successful(Done)

                override def commitJavadsl(): CompletionStage[Done] = CompletableFuture.completedFuture(Done)

                override def batchSize: Long = 1
              }
            }
        }
      }
}

object InMemoryKafkaBroker {
  case class InMemoryKafkaBrokerMessage(record: Record, recordMetadata: RecordMetadata)

  val TOPIC_PREFIX = "in-memory"

  val PARTITION_COUNT = 4

  val CONSUMER_GROUP_ID = "in-memory-consumer"
}
