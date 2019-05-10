package dao.authentication.model

import java.util.UUID

import org.joda.time.DateTime
import services.authentication.models.AuthenticationToken

case class SlickAuthenticationToken(
  sessionKey: String,
  userId: UUID,
  secretToken: UUID,
  createdAt: DateTime,
  expiresAt: DateTime
)

object SlickAuthenticationToken {
  def fromAuthenticationToken(authenticationToken: AuthenticationToken): SlickAuthenticationToken =
    SlickAuthenticationToken(
      AuthenticationToken.sessionToken(authenticationToken),
      authenticationToken.userId,
      authenticationToken.secretToken,
      authenticationToken.createdAt,
      authenticationToken.expiresAt
    )

  def toAuthenticationToken(slickAuthenticationToken: SlickAuthenticationToken): AuthenticationToken =
    AuthenticationToken(
      slickAuthenticationToken.userId,
      slickAuthenticationToken.secretToken,
      slickAuthenticationToken.createdAt,
      slickAuthenticationToken.expiresAt
    )
}
