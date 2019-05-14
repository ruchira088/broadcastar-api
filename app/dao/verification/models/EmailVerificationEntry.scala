package dao.verification.models

import java.util.UUID

import org.joda.time.DateTime

case class EmailVerificationEntry(
  userId: UUID,
  verificationToken: UUID,
  email: String,
  createdAt: DateTime,
  verifiedAt: Option[DateTime]
)
