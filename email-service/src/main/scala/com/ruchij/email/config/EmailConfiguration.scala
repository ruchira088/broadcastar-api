package com.ruchij.email.config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser._
import com.typesafe.config.Config

import scala.util.Try

case class EmailConfiguration(sendGridApiKey: String)

object EmailConfiguration {
  def parse(config: Config): Try[EmailConfiguration] =
    ConfigurationParser.parse[EmailConfiguration](config)
}
