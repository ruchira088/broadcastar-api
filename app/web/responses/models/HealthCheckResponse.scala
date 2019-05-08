package web.responses.models

import com.eed3si9n.ruchij.BuildInfo
import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}
import utils.SystemUtilities

import scala.util.Properties

case class HealthCheckResponse(
  serviceName: String,
  organization: String,
  version: String,
  javaVersion: String,
  sbtVersion: String,
  scalaVersion: String,
  timeStamp: DateTime,
  osName: String
)

object HealthCheckResponse {
  implicit val healthCheckWrites: OWrites[HealthCheckResponse] = Json.writes[HealthCheckResponse]

  def apply()(implicit systemUtilities: SystemUtilities): HealthCheckResponse =
    HealthCheckResponse(
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
