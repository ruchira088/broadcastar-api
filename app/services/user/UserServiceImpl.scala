package services.user

import java.util.UUID

import config.SystemUtilities
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import exceptions.{ExistingEmailException, ExistingUsernameException}
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.crypto.CryptographyService
import services.user.models.User
import utils.MonadicUtils.OptionTWrapper
import web.requests.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserServiceImpl @Inject()(databaseUserDao: DatabaseUserDao, cryptographyService: CryptographyService)(implicit systemUtilities: SystemUtilities)
    extends UserService {
  override def createUser(
    createUserRequest: CreateUserRequest
  )(implicit executionContext: ExecutionContext): Future[User] =
    for {
      _ <- Future.sequence {
        List(
          databaseUserDao.getByEmail(createUserRequest.email) ifNotEmpty ExistingEmailException(createUserRequest.email),
          databaseUserDao.getByUsername(createUserRequest.username) ifNotEmpty ExistingUsernameException(createUserRequest.username)
        )
      }

      saltedHashedPassword <- cryptographyService.hashPassword(createUserRequest.password)

      persistedUser <- databaseUserDao.insert(DatabaseUser.from(createUserRequest, saltedHashedPassword))

    } yield DatabaseUser.toUser(persistedUser)

  override def verifyEmail(userId: UUID, secret: String)(implicit executionContext: ExecutionContext): Future[User] =
    ???
}
