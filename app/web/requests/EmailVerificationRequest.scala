package web.requests

import java.util.UUID

import play.api.libs.json.{Json, Reads}

case class EmailVerificationRequest(verificationToken: UUID)

object EmailVerificationRequest {
  implicit val emailVerificationRequestReads: Reads[EmailVerificationRequest] =
    Json.reads[EmailVerificationRequest]
}
