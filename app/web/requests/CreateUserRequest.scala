package web.requests

import play.api.libs.json.{Json, OFormat}

case class CreateUserRequest(
  firstName: String,
  lastName: Option[String],
  password: String,
  email: String
)

object CreateUserRequest {
  implicit val createUserRequestFormat: OFormat[CreateUserRequest] = Json.format[CreateUserRequest]
}
