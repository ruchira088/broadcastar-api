package web.responses.models

import java.util.UUID

import play.api.libs.json.{Json, OWrites}

case class ForgotPasswordResponse(userId: UUID, email: String)

object ForgotPasswordResponse {
  implicit val forgotPasswordResponseWrites: OWrites[ForgotPasswordResponse] =
    Json.writes[ForgotPasswordResponse]
}
