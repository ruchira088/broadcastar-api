package com.ruchij.shared.kafka.models

import com.ruchij.shared.avro4s.AvroFormat.DateTimeAvroFormat
import com.ruchij.shared.models.{EmailVerificationToken, User}
import com.sksamuel.avro4s.RecordFormat
import play.api.libs.json.{Json, OFormat}

case class VerificationEmail(emailVerificationToken: EmailVerificationToken, user: User)

object VerificationEmail {
  implicit val verificationEmailFormat: OFormat[VerificationEmail] = Json.format[VerificationEmail]

  implicit val verificationEmailRecordFormat: RecordFormat[VerificationEmail] = RecordFormat[VerificationEmail]
}
