package services.authentication

import java.util.UUID

import com.ruchij.shared.models.{ResetPasswordToken, User}
import services.authentication.models.AuthenticationToken
import web.requests.models.{ResetPasswordRequest, UserLoginRequest}

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def createAuthenticationToken(userLoginRequest: UserLoginRequest)(
    implicit executionContext: ExecutionContext
  ): Future[AuthenticationToken]

  def getUserFromSessionToken(sessionToken: String)(implicit executionContext: ExecutionContext): Future[User]

  def extendExpiryTime(sessionToken: String)(implicit executionContext: ExecutionContext): Future[AuthenticationToken]

  def forgotPassword(email: String)(implicit executionContext: ExecutionContext): Future[ResetPasswordToken]

  def resetPassword(userId: UUID, resetPasswordRequest: ResetPasswordRequest)(implicit executionContext: ExecutionContext): Future[User]
}
