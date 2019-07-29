package com.ruchij.shared.kafka.config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.{intParser, stringConfigParser}
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class KafkaTopicConfiguration(topicPrefix: String, replicationFactor: Int, partitionCount: Int)

object KafkaTopicConfiguration {
  implicit val kafkaTopicConfigurationWrites: OWrites[KafkaTopicConfiguration] =
    Json.writes[KafkaTopicConfiguration]

  def parse(config: Config): Try[KafkaTopicConfiguration] =
    ConfigurationParser.parse[KafkaTopicConfiguration](config)
}
