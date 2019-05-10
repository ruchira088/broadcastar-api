package web.requests

import exceptions.ValidationException
import play.api.libs.json.{Json, OFormat}
import utils.MonadicUtils.{predicate, sequence, tryMonadError}

import scala.util.Try

case class UserLoginRequest(username: Option[String], email: Option[String], password: String)

object UserLoginRequest {
  implicit val userLoginRequestFormat: OFormat[UserLoginRequest] = Json.format[UserLoginRequest]

  implicit val userLoginRequestValidator: Validator[UserLoginRequest] = new Validator[UserLoginRequest] {
    override def validate[B <: UserLoginRequest](userLoginRequest: B): Try[B] =
      sequence(
        predicate[Try, Throwable](
          userLoginRequest.email.nonEmpty || userLoginRequest.username.nonEmpty,
          ValidationException("email and username, both cannot be empty")
        ),
        predicate[Try, Throwable](
          userLoginRequest.email.isEmpty || userLoginRequest.username.isEmpty,
          ValidationException("email and username, both cannot be populated")
        ),
        predicate[Try, Throwable](
          userLoginRequest.password.trim.nonEmpty,
          ValidationException("password cannot be empty")
        )
      ).map(_ => userLoginRequest)
  }
}
