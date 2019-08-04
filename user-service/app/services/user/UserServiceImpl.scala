package services.user

import java.util.UUID

import com.ruchij.shared.exceptions.ExistingResourceException
import com.ruchij.shared.exceptions.aggregation.AggregatedExistingResourceException
import com.ruchij.shared.models.{EmailVerificationToken, User}
import com.ruchij.shared.monads.MonadicUtils._
import com.ruchij.shared.utils.SystemUtilities
import com.typesafe.scalalogging.Logger
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import dao.verification.EmailVerificationTokenDao
import exceptions._
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.crypto.CryptographyService
import web.requests.models.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserServiceImpl @Inject()(
  databaseUserDao: DatabaseUserDao,
  emailVerificationTokenDao: EmailVerificationTokenDao,
  cryptographyService: CryptographyService
)(implicit systemUtilities: SystemUtilities)
    extends UserService {
  private val logger = Logger[UserServiceImpl]

  override def createUser(
    createUserRequest: CreateUserRequest
  )(implicit executionContext: ExecutionContext): Future[User] =
    for {
      exists <- sequence(
        databaseUserDao.getByEmail(createUserRequest.email) ifNotEmpty ExistingEmailException(createUserRequest.email),
        databaseUserDao.getByUsername(createUserRequest.username) ifNotEmpty ExistingUsernameException(
          createUserRequest.username
        )
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

      databaseUser = DatabaseUser.from(createUserRequest, saltedHashedPassword)

      emailVerificationToken <- emailVerificationTokenDao.insert(
        EmailVerificationToken(
          databaseUser.userId,
          systemUtilities.randomUuid(),
          -1,
          databaseUser.email,
          systemUtilities.currentTime(),
          None
        )
      )

      persistedUser <- databaseUserDao.insert(databaseUser)

    } yield DatabaseUser.toUser(persistedUser)

  override def usernameExists(username: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    databaseUserDao.getByUsername(username).nonEmpty

  override def verifyEmail(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): Future[User] =
    (emailVerificationTokenDao.verifyEmail(userId, secret) ifEmpty Future.failed {
      logger.warn(s"Email verification token not found (userId: $userId, secret: $secret)")
      ResourceNotFoundException("Email verification token not found")
    }).flatMap { _ =>
      withDefault(Future.failed(FatalDatabaseException)) {
        for {
          _ <- databaseUserDao.verifyEmail(userId)
          databaseUser <- databaseUserDao.getByUserId(userId)
        } yield DatabaseUser.toUser(databaseUser)
      }
    }

  override def getUserById(userId: UUID)(implicit executionContext: ExecutionContext): Future[User] =
    withDefault {
      logger.warn(s"User not found (id = $userId)")
      Future.failed(ResourceNotFoundException(s"User not found (id = $userId)"))
    } {
      databaseUserDao.getByUserId(userId).map(DatabaseUser.toUser)
    }

  override def getEmailVerificationToken(userId: UUID)(implicit executionContext: ExecutionContext): Future[EmailVerificationToken] =
    emailVerificationTokenDao.getByUserId(userId)
      .flatMap {
        _.headOption.fold[Future[EmailVerificationToken]] {
          logger.warn(s"Email verification token not found for user (id = $userId)")
          Future.failed(ResourceNotFoundException(s"Email verification token not found for user (id = $userId)"))
        } {
          Future.successful
        }
      }
}
