package services.user.models

import java.util.UUID

import org.joda.time.DateTime

case class User(
  id: UUID,
  createdAt: DateTime,
  firstName: String,
  lastName: Option[String],
  email: String
)
