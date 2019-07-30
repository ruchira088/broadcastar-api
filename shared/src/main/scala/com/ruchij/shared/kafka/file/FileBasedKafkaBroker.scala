package com.ruchij.shared.kafka.file

import java.util.concurrent.{CompletableFuture, CompletionStage}
import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.GroupTopicPartition
import akka.stream.scaladsl.Source
import com.ruchij.shared.json.JsonUtils
import com.ruchij.shared.kafka.config.FileBasedKafkaClientConfiguration
import com.ruchij.shared.kafka.consumer.KafkaConsumer
import com.ruchij.shared.kafka.file.models.FileBasedKafkaMessage
import com.ruchij.shared.kafka.producer.KafkaProducer
import com.ruchij.shared.kafka.{KafkaMessage, KafkaTopic}
import com.ruchij.shared.monads.MonadicUtils.tryMonadError
import com.ruchij.shared.utils.IOUtils.{lockFile, readFile, writeToFile}
import com.ruchij.shared.utils.{CollectionUtils, SystemUtilities}
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import play.api.libs.json.Json
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Random, Success, Try}

@Singleton
class FileBasedKafkaBroker @Inject()(fileBasedKafkaClientConfiguration: FileBasedKafkaClientConfiguration)(
  implicit systemUtilities: SystemUtilities,
  actorSystem: ActorSystem
) extends KafkaProducer
    with KafkaConsumer {

  val offset: AtomicInteger = new AtomicInteger(0)

  override def publish[A](
    message: KafkaMessage[A]
  )(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val fileBasedKafkaMessage = FileBasedKafkaMessage(message.kafkaTopic, message.value, offset.getAndIncrement())

    lockFile(
      fileBasedKafkaClientConfiguration.sourceFilePath,
      writeToFile(
        fileBasedKafkaClientConfiguration.sourceFilePath,
        (Json.asciiStringify {
          Json.toJson(fileBasedKafkaMessage) {
            FileBasedKafkaMessage.fileBasedKafkaMessageWrites(message.kafkaTopic.jsonFormat)
          }
        } + "\n").getBytes,
        append = true
      )
    ).map { _ =>
        new RecordMetadata(
          new TopicPartition(
            message.kafkaTopic.key,
            Random.nextInt(FileBasedKafkaBroker.PARTITION_COUNT)
          ),
          0,
          fileBasedKafkaMessage.offset.toLong,
          systemUtilities.currentTime().getMillis,
          message.hashCode().toLong,
          0,
          message.value.toString.length
        )
      }
  }

  override def subscribe[A](kafkaTopic: KafkaTopic[A])(
    implicit executionContext: ExecutionContext
  ): Source[(A, ConsumerMessage.CommittableOffset), _] = {
    val readOffset: AtomicInteger = new AtomicInteger(0)

    Source.tick(0 seconds, FileBasedKafkaBroker.POLL_INTERVAL, (): Unit)
      .mapAsync(parallelism = 1) {
        _ =>
          lockFile(
            fileBasedKafkaClientConfiguration.sourceFilePath,
            readFile(fileBasedKafkaClientConfiguration.sourceFilePath)
              .flatMap { bytes =>
                val lines = new String(bytes).split('\n').filter(_.trim.nonEmpty)

                OptionT {
                  Future.successful {
                    CollectionUtils.getIndex(lines, readOffset.get())
                  }
                }
              }
              .run
          )
      }
      .mapAsync(parallelism = 1) {
        currentLineOpt =>
          Future.fromTry {
            OptionT[Try, String] {
              Success(currentLineOpt)
            }
              .flatMapF {
                currentLine =>
                  readOffset.incrementAndGet()
                  Try(Json.parse(currentLine))
              }
              .flatMap {
                json =>
                  for {
                    topic <- OptionT[Try, KafkaTopic[_]] {
                      FileBasedKafkaMessage.resolveTopicFromJson(json).map(Option.apply)
                    }

                    _ <- if (topic.key == kafkaTopic.key) OptionT.some[Try, Unit]((): Unit) else OptionT.none[Try, Unit]

                    message <- OptionT {
                      JsonUtils.toTry {
                        Json.fromJson[FileBasedKafkaMessage[A]](json) {
                          FileBasedKafkaMessage.fileBasedKafkaMessageReads[A](kafkaTopic.jsonFormat, KafkaTopic.topicReads(kafkaTopic))
                        }
                      }
                        .map(Option.apply)
                    }
                  } yield message
              }
              .run
          }
      }
      .mapConcat {
        _.toList.map {
          case FileBasedKafkaMessage(topic, message, offset) =>
            (message, new ConsumerMessage.CommittableOffset {
              override def partitionOffset: ConsumerMessage.PartitionOffset =
                new ConsumerMessage.PartitionOffset(
                  GroupTopicPartition(
                    FileBasedKafkaBroker.CONSUMER_GROUP_ID,
                    topic.key,
                    Random.nextInt(FileBasedKafkaBroker.PARTITION_COUNT)
                  ),
                  offset
                )

              override def commitScaladsl(): Future[Done] = Future.successful(Done)

              override def commitJavadsl(): CompletionStage[Done] = CompletableFuture.completedFuture(Done)

              override def batchSize: Long = 1
            })
        }
      }
  }
}

object FileBasedKafkaBroker {
  val PARTITION_COUNT = 4

  val CONSUMER_GROUP_ID = "file-based-consumer"

  val POLL_INTERVAL: FiniteDuration = 100 milliseconds
}
