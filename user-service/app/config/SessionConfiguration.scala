package config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.finiteDurationConfigParser
import com.typesafe.config.Config
import com.ruchij.shared.json.JsonFormats.finiteDurationWrites
import play.api.libs.json.{Json, OWrites}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class SessionConfiguration(sessionDuration: FiniteDuration, passwordResetTokenDuration: FiniteDuration)

object SessionConfiguration {
  implicit val sessionConfiguration: OWrites[SessionConfiguration] =
    Json.writes[SessionConfiguration]

  def parse(config: Config): Try[SessionConfiguration] = ConfigurationParser.parse[SessionConfiguration](config)
}
