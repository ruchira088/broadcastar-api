package dao.verification.models

import java.util.UUID

import org.joda.time.DateTime

case class EmailVerificationToken(
  userId: UUID,
  secret: UUID,
  email: String,
  createdAt: DateTime,
  verifiedAt: Option[DateTime]
)
