package services.user.models

import java.util.UUID

import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

case class User(
  id: UUID,
  createdAt: DateTime,
  username: String,
  firstName: String,
  lastName: Option[String],
  email: String
)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
