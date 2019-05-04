package services.user

import java.util.UUID

import services.user.models.User
import web.requests.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  def createUser(createUserRequest: CreateUserRequest)(implicit executionContext: ExecutionContext): Future[User]

  def usernameExists(username: String)(implicit executionContext: ExecutionContext): Future[Boolean]

  def verifyEmail(userId: UUID, secret: String)(implicit executionContext: ExecutionContext): Future[User]
}
