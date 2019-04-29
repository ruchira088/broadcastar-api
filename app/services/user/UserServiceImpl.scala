package services.user

import java.util.UUID

import javax.inject.Singleton
import services.user.models.User
import web.requests.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserServiceImpl extends UserService {
  override def create(createUserRequest: CreateUserRequest)(implicit executionContext: ExecutionContext): Future[User] = ???

  override def verifyEmail(userId: UUID, secret: String)(implicit executionContext: ExecutionContext): Future[User] = ???

  override def getByEmail(email: String)(implicit executionContext: ExecutionContext): Future[User] = ???
}
