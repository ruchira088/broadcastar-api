package com.ruchij.email.config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.{secretParser, stringConfigParser}
import com.ruchij.shared.config.models.Secret
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class EmailConfiguration(sendGridApiKey: Secret[String])

object EmailConfiguration {
  implicit val emailConfigurationWrites: OWrites[EmailConfiguration] =
    Json.writes[EmailConfiguration]

  def parse(config: Config): Try[EmailConfiguration] =
    ConfigurationParser.parse[EmailConfiguration](config)
}
