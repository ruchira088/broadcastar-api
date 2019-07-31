package com.ruchij.shared.models

import java.util.UUID

import com.ruchij.shared.avro4s.AvroFormat.DateTimeAvroFormat
import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import com.sksamuel.avro4s.RecordFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

case class ResetPasswordToken(
  userId: UUID,
  secret: UUID,
  createdAt: DateTime,
  index: Long,
  email: String,
  expiresAt: DateTime,
  resetAt: Option[DateTime]
)

object ResetPasswordToken {
  implicit val resetPasswordTokenFormat: OFormat[ResetPasswordToken] = Json.format[ResetPasswordToken]

  implicit val resetPasswordTokenRecordFormat: RecordFormat[ResetPasswordToken] = RecordFormat[ResetPasswordToken]
}