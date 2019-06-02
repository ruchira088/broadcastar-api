package web.requests.models

import com.ruchij.shared.web.requests.Validator
import play.api.libs.json.{Json, Reads}

import scala.util.Try

case class ForgotPasswordRequest(email: String)

object ForgotPasswordRequest {
  implicit val forgotPasswordRequestReads: Reads[ForgotPasswordRequest] = Json.reads[ForgotPasswordRequest]

  implicit val forgotPasswordRequestValidator: Validator[ForgotPasswordRequest] = new Validator[ForgotPasswordRequest] {
    override def validate[B <: ForgotPasswordRequest](forgotPasswordRequest: B): Try[B] =
      Validator.emailValidator(forgotPasswordRequest.email)
        .map(_ => forgotPasswordRequest)
  }
}
