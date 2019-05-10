package services.authentication

import services.authentication.models.AuthenticationToken
import services.user.models.User
import web.requests.UserLoginRequest

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def createAuthenticationToken(userLoginRequest: UserLoginRequest)(
    implicit executionContext: ExecutionContext
  ): Future[AuthenticationToken]

  def getUserFromSessionToken(sessionToken: String)(implicit executionContext: ExecutionContext): Future[User]

  def extendExpiryTime(sessionToken: String)(implicit executionContext: ExecutionContext): Future[AuthenticationToken]
}
