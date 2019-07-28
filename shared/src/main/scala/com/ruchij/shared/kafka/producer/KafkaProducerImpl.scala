package com.ruchij.shared.kafka.producer

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import com.ruchij.shared.kafka.KafkaUtils.{commonClientProperties, schemaRegistryConfiguration}
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.KafkaMessage
import io.confluent.kafka.serializers.KafkaAvroSerializer
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.{Callback, Producer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class KafkaProducerImpl @Inject()(kafkaConfiguration: KafkaConfiguration)(implicit actorSystem: ActorSystem) extends KafkaProducer {
  lazy val kafkaProducer: Producer[String, AnyRef] = KafkaProducerImpl.settings(kafkaConfiguration).createKafkaProducer()

  override def publish[A](message: KafkaMessage[A])(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val promise = Promise[RecordMetadata]

      kafkaProducer.send(
        new ProducerRecord[String, AnyRef](message.kafkaTopic.name(kafkaConfiguration.topicPrefix), message.kafkaTopic.recordFormat.to(message.value)),
        new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit =
            Option(exception).fold(promise.success(metadata))(promise.failure)
        }
      )

    promise.future
  }
}

object KafkaProducerImpl {
  def settings(kafkaConfiguration: KafkaConfiguration)(implicit actorSystem: ActorSystem): ProducerSettings[String, AnyRef] =
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
