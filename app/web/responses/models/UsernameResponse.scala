package web.responses.models

import play.api.libs.json.{Json, OWrites}

case class UsernameResponse(username: String, exists: Boolean)

object UsernameResponse {
  implicit val usernameResponseWrites: OWrites[UsernameResponse] = Json.writes[UsernameResponse]
}
