package web.requests.models

import com.ruchij.shared.web.requests.Validator
import play.api.libs.json.{Json, OFormat}
import com.ruchij.shared.web.requests.Validator.{combine, validate => validator}

import scala.util.Try

case class CreateUserRequest(
  username: String,
  firstName: String,
  lastName: Option[String],
  profileImageId: Option[String],
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
        Validator.passwordValidator.compose(_.password),
        Validator.emailValidator.compose(_.email)
      )
  }
}
