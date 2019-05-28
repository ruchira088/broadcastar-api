package config

import com.ruchij.config.{ConfigParser, ConfigPathTransformer}
import com.typesafe.config.Config
import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class ApplicationInformation(
  gitBranch: Option[String],
  gitCommit: Option[String],
  dockerBuildTimestamp: Option[DateTime]
)

object ApplicationInformation {
  implicit val configPathTransformer: ConfigPathTransformer = "application"

  implicit val applicationInformationWrites: OWrites[ApplicationInformation] = Json.writes[ApplicationInformation]

  def parse(config: Config): Try[ApplicationInformation] = ConfigParser.parse[ApplicationInformation](config)
}
