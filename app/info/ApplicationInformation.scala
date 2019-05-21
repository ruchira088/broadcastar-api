package info

import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}

case class ApplicationInformation(gitBranch: Option[String], gitCommit: Option[String], buildTimestamp: Option[DateTime])

object ApplicationInformation {
  implicit val applicationInformationWrites: OWrites[ApplicationInformation] = Json.writes[ApplicationInformation]
}
