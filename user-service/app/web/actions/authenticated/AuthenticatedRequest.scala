package web.actions.authenticated

import com.ruchij.shared.models.User
import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[+A](authenticatedUser: User, request: Request[A]) extends WrappedRequest[A](request)
