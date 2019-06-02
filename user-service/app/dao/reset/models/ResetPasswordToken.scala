package dao.reset.models

import java.util.UUID

import org.joda.time.DateTime

case class ResetPasswordToken(
  userId: UUID,
  secret: UUID,
  createdAt: DateTime,
  email: String,
  expiresAt: DateTime,
  resetAt: Option[DateTime]
)
