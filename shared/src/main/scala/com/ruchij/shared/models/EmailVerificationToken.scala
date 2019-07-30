package com.ruchij.shared.models

import java.util.UUID

import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OFormat}

case class EmailVerificationToken(
  userId: UUID,
  secret: UUID,
  email: String,
  createdAt: DateTime,
  verifiedAt: Option[DateTime]
)

object EmailVerificationToken {
  implicit val emailVerificationTokenFormat: OFormat[EmailVerificationToken] = Json.format[EmailVerificationToken]
}
