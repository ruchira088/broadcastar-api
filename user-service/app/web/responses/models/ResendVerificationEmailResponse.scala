package web.responses.models

import play.api.libs.json.{Json, OWrites}

case class ResendVerificationEmailResponse(email: String)

object ResendVerificationEmailResponse {
  implicit val resendVerificationEmailResponseWrite: OWrites[ResendVerificationEmailResponse] =
    Json.writes[ResendVerificationEmailResponse]
}
