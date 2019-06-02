package com.ruchij.shared.info

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.{optionParser, stringConfigParser, dateTimeParser}
import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import com.typesafe.config.Config
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class BuildInformation(
  gitBranch: Option[String],
  gitCommit: Option[String],
  dockerBuildTimestamp: Option[DateTime]
)

object BuildInformation {
  implicit val buildInformationWrites: OWrites[BuildInformation] = Json.writes[BuildInformation]

  def parse(config: Config): Try[BuildInformation] = ConfigurationParser.parse[BuildInformation](config)
}
