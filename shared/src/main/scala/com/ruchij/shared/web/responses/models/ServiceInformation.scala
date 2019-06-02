package com.ruchij.shared.web.responses.models

import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}

case class ServiceInformation(
  serviceName: String,
  organization: String,
  version: String,
  javaVersion: String,
  sbtVersion: String,
  scalaVersion: String,
  timestamp: DateTime,
  osName: String,
  gitCommit: Option[String],
  gitBranch: Option[String],
  dockerBuildTimestamp: Option[DateTime]
)

object ServiceInformation {
  implicit val healthCheckWrites: OWrites[ServiceInformation] = Json.writes[ServiceInformation]
}
