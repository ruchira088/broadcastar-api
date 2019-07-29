package com.ruchij.shared.kafka.config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.{optionParser, secretParser, stringConfigParser}
import com.ruchij.shared.config.models.Secret
import com.typesafe.config.Config
import play.api.libs.json.{JsObject, Json, OWrites}

import scala.util.Try

sealed trait KafkaClientConfiguration

object KafkaClientConfiguration {
  case class LocalKafkaClientConfiguration(
    bootstrapServers: String,
    schemaRegistryUrl: String,
    consumerGroupId: Option[String]
  ) extends KafkaClientConfiguration

  case class ConfluentKafkaClientConfiguration(
    bootstrapServers: String,
    schemaRegistryUrl: String,
    consumerGroupId: Option[String],
    kafkaUsername: Secret[String],
    kafkaPassword: Secret[String],
    schemaRegistryUsername: Secret[String],
    schemaRegistryPassword: Secret[String]
  ) extends KafkaClientConfiguration

  implicit val kafkaClientConfigurationWrites: OWrites[KafkaClientConfiguration] = {
    case localKafkaClientConfiguration: LocalKafkaClientConfiguration => Json.writes[LocalKafkaClientConfiguration].writes(localKafkaClientConfiguration)
    case confluentKafkaClientConfiguration: ConfluentKafkaClientConfiguration =>
      Json.writes[ConfluentKafkaClientConfiguration].writes(confluentKafkaClientConfiguration)
  }

  def parseLocalConfig(config: Config): Try[LocalKafkaClientConfiguration] =
    ConfigurationParser.parse[LocalKafkaClientConfiguration](config)

  def parseConfluentConfig(config: Config): Try[ConfluentKafkaClientConfiguration] =
    ConfigurationParser.parse[ConfluentKafkaClientConfiguration](config)

  def consumerGroupId: PartialFunction[KafkaClientConfiguration, Option[String]] = {
    case LocalKafkaClientConfiguration(_, _, consumerGroupId) => consumerGroupId
    case confluentKafkaConfiguration: ConfluentKafkaClientConfiguration => confluentKafkaConfiguration.consumerGroupId
  }
}
