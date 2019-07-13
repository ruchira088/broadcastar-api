package com.ruchij.shared.kafka.models

import com.ruchij.shared.avro4s.AvroFormat.DateTimeFormat
import com.ruchij.shared.models.{EmailVerificationToken, User}
import com.sksamuel.avro4s.RecordFormat

case class VerificationEmail(emailVerificationToken: EmailVerificationToken, user: User)

object VerificationEmail {
  implicit val verificationEmailRecordFormat: RecordFormat[VerificationEmail] = RecordFormat[VerificationEmail]
}
