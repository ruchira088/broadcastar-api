package config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.finiteDurationConfigParser
import com.typesafe.config.Config
import com.ruchij.shared.json.JsonFormats.finiteDurationWrites
import play.api.libs.json.{Json, OWrites}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class AuthenticationConfiguration(sessionDuration: FiniteDuration, passwordResetTokenDuration: FiniteDuration)

object AuthenticationConfiguration {
  implicit val authenticationConfigurationWrites: OWrites[AuthenticationConfiguration] =
    Json.writes[AuthenticationConfiguration]

  def parse(config: Config): Try[AuthenticationConfiguration] = ConfigurationParser.parse[AuthenticationConfiguration](config)
}
