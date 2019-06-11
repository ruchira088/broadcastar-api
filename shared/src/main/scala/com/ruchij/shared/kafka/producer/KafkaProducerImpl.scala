package com.ruchij.shared.kafka.producer

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.KafkaMessage
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroSerializer}
import javax.inject.{Inject, Singleton}
import org.apache.kafka.clients.producer.{Callback, Producer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class KafkaProducerImpl @Inject()(producerSettings: ProducerSettings[String, AnyRef]) extends KafkaProducer {
  lazy val kafkaProducer: Producer[String, AnyRef] = producerSettings.createKafkaProducer()

  override def publish[A](message: KafkaMessage[A])(implicit executionContext: ExecutionContext): Future[RecordMetadata] = {
    val promise = Promise[RecordMetadata]

      kafkaProducer.send(
        new ProducerRecord[String, AnyRef](message.kafkaTopic.name, message.kafkaTopic.recordFormat.to(message.value)),
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
          Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaConfiguration.schemaRegistryUrl).asJava,
          false
        )
      }
    )
      .withBootstrapServers(kafkaConfiguration.bootstrapServers)
}
