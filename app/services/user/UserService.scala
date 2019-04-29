package services.user

import java.util.UUID

import services.user.models.User
import web.requests.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

trait UserService {
  def create(createUserRequest: CreateUserRequest)(implicit executionContext: ExecutionContext): Future[User]

  def verifyEmail(userId: UUID, secret: String)(implicit executionContext: ExecutionContext): Future[User]

  def getByEmail(email: String)(implicit executionContext: ExecutionContext): Future[User]
}
