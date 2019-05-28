package config

import java.nio.file.Path

import com.ruchij.config.ConfigParser
import com.typesafe.config.Config
import json.JsonFormats.pathWrites
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class LocalFileStoreConfiguration(storePath: Path)

object LocalFileStoreConfiguration {
  implicit val localFileStoreConfigurationWrites: OWrites[LocalFileStoreConfiguration] =
    Json.writes[LocalFileStoreConfiguration]

  def parse(config: Config): Try[LocalFileStoreConfiguration] = ConfigParser.parse[LocalFileStoreConfiguration](config)
}
