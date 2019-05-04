package services.authentication.models

import java.util.UUID

import org.joda.time.DateTime

case class AuthenticationToken(id: UUID, createdAt: DateTime, userId: UUID, expiresAt: DateTime)
