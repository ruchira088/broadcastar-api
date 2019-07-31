package com.ruchij.shared.models

import java.util.UUID

import com.ruchij.shared.avro4s.AvroFormat.DateTimeAvroFormat
import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import com.sksamuel.avro4s.RecordFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

case class User(
  userId: UUID,
  createdAt: DateTime,
  username: String,
  firstName: String,
  lastName: Option[String],
  email: String,
  profileImageId: Option[String]
)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]

  implicit val userRecordFormat: RecordFormat[User] = RecordFormat[User]
}
