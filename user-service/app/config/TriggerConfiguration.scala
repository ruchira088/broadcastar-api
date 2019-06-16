package config

import com.ruchij.shared.config.ConfigurationParser
import com.ruchij.shared.config.ConfigurationParser.finiteDurationConfigParser
import com.ruchij.shared.json.JsonFormats.finiteDurationWrites
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

case class TriggerConfiguration(
  offsetLockTimeout: FiniteDuration,
  initialDelay: FiniteDuration,
  pollingInterval: FiniteDuration
)

object TriggerConfiguration {
  implicit val triggerConfigurationWrites: OWrites[TriggerConfiguration] = Json.writes[TriggerConfiguration]

  def parse(config: Config): Try[TriggerConfiguration] = ConfigurationParser.parse[TriggerConfiguration](config)
}
