package config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.stringConfigParser
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class S3Configuration(s3Bucket: String)

object S3Configuration {
  implicit val s3ConfigurationWrites: OWrites[S3Configuration] = Json.writes[S3Configuration]

  def parse(config: Config): Try[S3Configuration] = ConfigurationParser.parse[S3Configuration](config)
}
