package web.requests.models

import java.util.UUID

import play.api.libs.json.{Json, Reads}
import web.requests.Validator

import scala.util.Try

case class ResetPasswordRequest(secret: UUID, password: String)

object ResetPasswordRequest {
  implicit val resetPasswordRequestReads: Reads[ResetPasswordRequest] = Json.reads[ResetPasswordRequest]

  implicit val resetPasswordRequestValidator: Validator[ResetPasswordRequest] = new Validator[ResetPasswordRequest] {
    override def validate[B <: ResetPasswordRequest](resetPasswordRequest: B): Try[B] =
      Validator.passwordValidator(resetPasswordRequest.password)
        .map(_ => resetPasswordRequest)
  }
}
