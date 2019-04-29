package dao.user.models

import java.util.UUID

import org.joda.time.DateTime

case class DatabaseUser(
  id: UUID,
  createdAt: DateTime,
  firstName: String,
  lastName: Option[String],
  email: String,
  password: String,
  emailVerified: Boolean
)
