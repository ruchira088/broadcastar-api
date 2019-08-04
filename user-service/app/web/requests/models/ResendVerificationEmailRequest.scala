package web.requests.models

import com.ruchij.shared.web.requests.Validator
import play.api.libs.json.{Json, Reads}

import scala.util.Try

case class ResendVerificationEmailRequest(email: String)

object ResendVerificationEmailRequest {
  implicit val resendVerificationEmailRequestReads: Reads[ResendVerificationEmailRequest] =
    Json.reads[ResendVerificationEmailRequest]

  implicit val resendVerificationEmailRequestValidator: Validator[ResendVerificationEmailRequest] =
    new Validator[ResendVerificationEmailRequest] {
      override def validate[B <: ResendVerificationEmailRequest](value: B): Try[B] =
        Validator.emailValidator(value.email).map(_ => value)
    }
}
