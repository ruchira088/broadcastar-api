package com.ruchij.shared.config

import com.typesafe.config.Config
import play.api.libs.json.{Json, Writes}
import ConfigurationParser.stringConfigParser

import scala.util.Try

case class KafkaConfiguration(bootstrapServers: String, schemaRegistryUrl: String, consumerGroupId: String)

object KafkaConfiguration {
  implicit val kafkaConfigurationWrites: Writes[KafkaConfiguration] = Json.writes[KafkaConfiguration]

  def parse(config: Config): Try[KafkaConfiguration] =
    ConfigurationParser.parse[KafkaConfiguration](config)
}
