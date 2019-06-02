package web.actions.authenticated

import play.api.mvc.{Request, WrappedRequest}
import services.user.models.User

case class AuthenticatedRequest[+A](authenticatedUser: User, request: Request[A]) extends WrappedRequest[A](request)
