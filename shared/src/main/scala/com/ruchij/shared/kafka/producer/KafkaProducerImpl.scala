package com.ruchij.shared.kafka.producer

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import com.ruchij.shared.kafka.KafkaUtils.{commonClientProperties, schemaRegistryConfiguration}
import com.ruchij.shared.kafka.KafkaMessage
import com.ruchij.shared.kafka.config.{KafkaClientConfiguration, KafkaTopicConfiguration}
import com.typesafe.scalalogging.Logger
import io.confluent.kafka.serializers.KafkaAvroSerializer
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.{Callback, Producer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

@Singleton
class KafkaProducerImpl @Inject()(kafkaConfiguration: KafkaClientConfiguration, kafkaTopicConfiguration: KafkaTopicConfiguration)(implicit actorSystem: ActorSystem) extends KafkaProducer {
  private val logger = Logger[KafkaProducerImpl]

  lazy val kafkaProducer: Producer[String, AnyRef] = KafkaProducerImpl.settings(kafkaConfiguration).createKafkaProducer()

  override def publish[A](message: KafkaMessage[A])(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val promise = Promise[RecordMetadata]

    Try {
      kafkaProducer.send(
        new ProducerRecord[String, AnyRef](message.kafkaTopic.name(kafkaTopicConfiguration.topicPrefix), message.kafkaTopic.recordFormat.to(message.value)),
        new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
            Option(exception).fold(promise.success(metadata)) {
              _ =>
                logger.error(exception.getMessage, exception)
                promise.failure(exception)
            }
        }
      )
    }
        .recover {
          case throwable: Throwable =>
            logger.error(throwable.getMessage, throwable)
            promise.failure(throwable)
        }

    promise.future
  }
}

object KafkaProducerImpl {
  def settings(kafkaConfiguration: KafkaClientConfiguration)(implicit actorSystem: ActorSystem): ProducerSettings[String, AnyRef] =
    ProducerSettings(
      actorSystem,
      new StringSerializer,
      new KafkaAvroSerializer() {
        configure(
          schemaRegistryConfiguration(kafkaConfiguration).asJava,
          false
        )
      }
    )
      .withProperties(commonClientProperties(kafkaConfiguration))
}
