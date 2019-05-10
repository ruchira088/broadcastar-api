package services.authentication.models

import java.util.UUID

import org.joda.time.DateTime

case class AuthenticationToken(userId: UUID, secretToken: UUID, createdAt: DateTime, expiresAt: DateTime)

object AuthenticationToken {
  def sessionToken(authenticationToken: AuthenticationToken): String =
    s"${authenticationToken.userId}-${authenticationToken.secretToken}"
}
