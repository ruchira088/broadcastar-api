package web.responses.models

import config.ApplicationInformation
import info.BuildInfo
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
  osName: String,
  gitCommit: Option[String],
  gitBranch: Option[String],
  dockerBuildTimestamp: Option[DateTime]
)

object HealthCheckResponse {
  implicit val healthCheckWrites: OWrites[HealthCheckResponse] = Json.writes[HealthCheckResponse]

  def apply()(implicit systemUtilities: SystemUtilities, applicationInformation: ApplicationInformation): HealthCheckResponse =
    HealthCheckResponse(
      BuildInfo.name,
      BuildInfo.organization,
      BuildInfo.version,
      Properties.javaVersion,
      BuildInfo.sbtVersion,
      BuildInfo.scalaVersion,
      systemUtilities.currentTime(),
      Properties.osName,
      applicationInformation.gitCommit,
      applicationInformation.gitBranch,
      applicationInformation.dockerBuildTimestamp
    )
}
