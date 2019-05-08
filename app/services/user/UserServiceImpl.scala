package services.user

import java.util.UUID

import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import exceptions.aggregation.AggregatedExistingResourceException
import exceptions.{ExistingEmailException, ExistingResourceException, ExistingUsernameException}
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.crypto.CryptographyService
import services.user.models.User
import utils.{MonadicUtils, SystemUtilities}
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
      exists <- MonadicUtils.sequence(
        databaseUserDao.getByEmail(createUserRequest.email) ifNotEmpty ExistingEmailException(createUserRequest.email),
        databaseUserDao.getByUsername(createUserRequest.username) ifNotEmpty ExistingUsernameException(createUserRequest.username)
      )

      _ <- exists.fold(
        errors =>
          Future.failed {
            AggregatedExistingResourceException {
              errors.collect { case existingResourceException: ExistingResourceException => existingResourceException }
            }
          },
        _ => Future.successful((): Unit)
      )


      saltedHashedPassword <- cryptographyService.hashPassword(createUserRequest.password)

      persistedUser <- databaseUserDao.insert(DatabaseUser.from(createUserRequest, saltedHashedPassword))

    } yield DatabaseUser.toUser(persistedUser)

  override def usernameExists(username: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    databaseUserDao.getByUsername(username).nonEmpty

  override def verifyEmail(userId: UUID, secret: String)(implicit executionContext: ExecutionContext): Future[User] =
    ???
}
