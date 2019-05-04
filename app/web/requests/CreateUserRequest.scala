package web.requests

import org.apache.commons.validator.routines.EmailValidator
import play.api.libs.json.{Json, OFormat}
import web.requests.Validator.{combine, validate => validator}

import scala.util.Try

case class CreateUserRequest(
  username: String,
  firstName: String,
  lastName: Option[String],
  password: String,
  email: String
)

object CreateUserRequest {
  implicit val createUserRequestFormat: OFormat[CreateUserRequest] = Json.format[CreateUserRequest]

  implicit val createUserRequestValidator: Validator[CreateUserRequest] = new Validator[CreateUserRequest] {
    override def validate[B <: CreateUserRequest](createUserRequest: B): Try[B] =
      combine(createUserRequest) (
        validator(_.username.trim.nonEmpty, "username must NOT be empty"),
        validator(_.firstName.trim.nonEmpty, "firstName must NOT be empty"),
        validator(_.password.trim.length > 8, "password length must be greater than 8 characters"),
        validator(_ => EmailValidator.getInstance().isValid(createUserRequest.email), s"${createUserRequest.email} is NOT a valid email address"),
      )
  }
}
