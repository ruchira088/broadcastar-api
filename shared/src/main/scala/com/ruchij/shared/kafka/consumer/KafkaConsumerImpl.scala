package com.ruchij.shared.kafka.consumer

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Source
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.exceptions.FatalException
import com.ruchij.shared.kafka.KafkaUtils.{commonClientProperties, schemaRegistryConfiguration}
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.utils.SystemUtilities
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import javax.inject.{Inject, Singleton}
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.common.serialization.StringDeserializer

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class KafkaConsumerImpl @Inject()(kafkaConfiguration: KafkaConfiguration)(implicit actorSystem: ActorSystem, systemUtilities: SystemUtilities) extends KafkaConsumer {
  override def subscribe[A](
    kafkaTopic: KafkaTopic[A]
  )(implicit executionContext: ExecutionContext): Source[(A, CommittableOffset), _] =
    Consumer
      .committableSource(KafkaConsumerImpl.settings(kafkaConfiguration), Subscriptions.topics(kafkaTopic.name(kafkaConfiguration)))
      .map { committableMessage => (committableMessage.record.value(), committableMessage.committableOffset) }
      .mapAsync(1) {
        case (genericRecord: GenericRecord, committableOffset) =>
          Future.fromTry { Try(kafkaTopic.recordFormat.from(genericRecord)) }
            .map { (_, committableOffset) }

        case _ =>
          Future.failed {
            FatalException("Unexpected kafka message type")
          }
      }
}

object KafkaConsumerImpl {
  def settings(
    kafkaConfiguration: KafkaConfiguration
  )(implicit actorSystem: ActorSystem, systemUtilities: SystemUtilities): ConsumerSettings[String, AnyRef] =
    ConsumerSettings(
      actorSystem,
      new StringDeserializer,
      new KafkaAvroDeserializer() {
        configure(
          schemaRegistryConfiguration(kafkaConfiguration).asJava,
          false
        )
      }
    )
      .withProperties(commonClientProperties(kafkaConfiguration))
      .withGroupId(kafkaConfiguration.consumerGroupId.getOrElse(systemUtilities.randomUuid().toString))
}
