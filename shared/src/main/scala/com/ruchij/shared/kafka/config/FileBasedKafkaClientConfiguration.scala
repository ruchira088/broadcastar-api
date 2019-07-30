package com.ruchij.shared.kafka.config

import java.nio.file.Path

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.pathConfigParser
import com.typesafe.config.Config

import scala.util.Try

case class FileBasedKafkaClientConfiguration(sourceFilePath: Path)

object FileBasedKafkaClientConfiguration {
  def parse(config: Config): Try[FileBasedKafkaClientConfiguration] =
    ConfigurationParser.parse[FileBasedKafkaClientConfiguration](config)
}
