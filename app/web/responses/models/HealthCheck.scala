package web.responses.models

import com.eed3si9n.ruchij.BuildInfo
import config.SystemUtilities
import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

import scala.util.Properties

case class HealthCheck(
  serviceName: String,
  organization: String,
  version: String,
  javaVersion: String,
  sbtVersion: String,
  scalaVersion: String,
  timeStamp: DateTime,
  osName: String
)

object HealthCheck {
  implicit val healthCheckFormat: OFormat[HealthCheck] =
    Json.format[HealthCheck]

  def apply()(implicit systemUtilities: SystemUtilities): HealthCheck =
    HealthCheck(
      BuildInfo.name,
      BuildInfo.organization,
      BuildInfo.version,
      Properties.javaVersion,
      BuildInfo.sbtVersion,
      BuildInfo.scalaVersion,
      systemUtilities.currentTime(),
      Properties.osName
    )
}
