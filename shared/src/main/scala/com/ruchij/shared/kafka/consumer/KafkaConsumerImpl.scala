package com.ruchij.shared.kafka.consumer

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.KafkaTopic
import io.confluent.kafka.serializers.{AbstractKafkaAvroSerDeConfig, KafkaAvroDeserializer}
import javax.inject.{Inject, Singleton}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.ExecutionContext

@Singleton
class KafkaConsumerImpl @Inject()(kafkaConfiguration: KafkaConfiguration)(implicit actorSystem: ActorSystem) extends KafkaConsumer {
  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, CommittableOffset), _] =
    Consumer
      .committableSource(KafkaConsumerImpl.settings(kafkaConfiguration), Subscriptions.topics(kafkaTopic.name(kafkaConfiguration)))
      .map { committableMessage => (committableMessage.record.value(), committableMessage.committableOffset) }
      .collect {
        case (genericRecord: GenericRecord, committableOffset: CommittableOffset) =>
          (kafkaTopic.recordFormat.from(genericRecord), committableOffset)
      }
}

object KafkaConsumerImpl {
  def settings(
    kafkaConfiguration: KafkaConfiguration
  )(implicit actorSystem: ActorSystem): ConsumerSettings[String, AnyRef] =
    ConsumerSettings(
      actorSystem,
      new StringDeserializer,
      new KafkaAvroDeserializer() {
        configure(
          Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaConfiguration.schemaRegistryUrl).asJava,
          false
        )
      }
    )
      .withBootstrapServers(kafkaConfiguration.bootstrapServers)
      .withGroupId(kafkaConfiguration.consumerGroupId)
}
