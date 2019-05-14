package web.actions.user

import play.api.mvc.{Request, WrappedRequest}
import services.user.models.User

case class UserRequest[+A](user: User, request: Request[A]) extends WrappedRequest[A](request)
