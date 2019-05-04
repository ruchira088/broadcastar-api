package web.responses.models

import play.api.libs.json.{Json, OFormat}

case class UsernameResponse(username: String, exists: Boolean)

object UsernameResponse {
  implicit val usernameResponseFormat: OFormat[UsernameResponse] = Json.format[UsernameResponse]
}
