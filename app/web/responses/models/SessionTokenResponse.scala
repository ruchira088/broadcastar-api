package web.responses.models

import json.JsonFormats.DateTimeFormat
import org.joda.time.DateTime
import play.api.libs.json.{Json, OWrites}
import services.authentication.models.AuthenticationToken

case class SessionTokenResponse(sessionToken: String, expiresAt: DateTime)

object SessionTokenResponse {
  implicit val sessionTokenResponseWrites: OWrites[SessionTokenResponse] = Json.writes[SessionTokenResponse]

  def fromAuthenticationToken(authenticationToken: AuthenticationToken): SessionTokenResponse =
    SessionTokenResponse(AuthenticationToken.sessionToken(authenticationToken), authenticationToken.expiresAt)
}
