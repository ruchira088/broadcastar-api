package services.user

import java.util.UUID

import com.ruchij.shared.models.{EmailVerificationToken, User}
import web.requests.models.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  def createUser(createUserRequest: CreateUserRequest)(implicit executionContext: ExecutionContext): Future[User]

  def usernameExists(username: String)(implicit executionContext: ExecutionContext): Future[Boolean]

  def verifyEmail(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): Future[User]

  def getUserById(userId: UUID)(implicit executionContext: ExecutionContext): Future[User]

  def getEmailVerificationToken(userId: UUID)(implicit executionContext: ExecutionContext): Future[EmailVerificationToken]

  def resendVerificationEmail(email: String)(implicit executionContext: ExecutionContext): Future[EmailVerificationToken]
}
