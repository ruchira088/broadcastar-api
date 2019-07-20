package com.ruchij.shared.config

import com.typesafe.config.Config
import play.api.libs.json.{Json, Writes}
import ConfigurationParser.{intParser, optionParser, stringConfigParser, secretParser}
import com.ruchij.shared.config.models.Secret

import scala.util.Try

case class KafkaConfiguration(
  bootstrapServers: String,
  schemaRegistryUrl: String,
  topicPrefix: String,
  consumerGroupId: Option[String],
  kafkaUsername: Secret[String],
  kafkaPassword: Secret[String],
  schemaRegistryUsername: Secret[String],
  schemaRegistryPassword: Secret[String],
  topicReplicationFactor: Int,
  topicPartitionCount: Int
)

object KafkaConfiguration {
  implicit val kafkaConfigurationWrites: Writes[KafkaConfiguration] = Json.writes[KafkaConfiguration]

  def parse(config: Config): Try[KafkaConfiguration] =
    ConfigurationParser.parse[KafkaConfiguration](config)
}
