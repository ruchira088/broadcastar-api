package com.ruchij.shared.config

import com.ruchij.macros.config.ConfigParser
import com.ruchij.shared.config.ConfigurationParser.{enumParser, stringConfigParser}
import com.ruchij.shared.config.models.DevelopmentMode
import com.typesafe.config.Config

import scala.util.Try

case class DevelopmentConfiguration(developmentMode: DevelopmentMode)

object DevelopmentConfiguration {
  implicit val developmentModeParser: ConfigParser[DevelopmentMode] = enumParser[DevelopmentMode]

  def parse(config: Config): Try[DevelopmentConfiguration] =
    ConfigurationParser.parse[DevelopmentConfiguration](config)
}
