package services.user

import java.util.UUID

import com.ruchij.shared.exceptions.ExistingResourceException
import com.ruchij.shared.utils.MonadicUtils._
import com.ruchij.shared.utils.SystemUtilities
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import dao.verification.EmailVerificationTokenDao
import dao.verification.models.EmailVerificationToken
import exceptions._
import com.ruchij.shared.exceptions.aggregation.AggregatedExistingResourceException
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.crypto.CryptographyService
import services.notification.NotificationService
import services.notification.models.NotificationType.Console
import services.notification.console.models.ConsoleNotificationMessage._
import services.user.models.User
import web.requests.models.CreateUserRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserServiceImpl @Inject()(
  databaseUserDao: DatabaseUserDao,
  emailVerificationTokenDao: EmailVerificationTokenDao,
  notificationService: NotificationService[Console.type],
  cryptographyService: CryptographyService
)(implicit systemUtilities: SystemUtilities)
    extends UserService {
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

      persistedUser <- databaseUserDao.insert(DatabaseUser.from(createUserRequest, saltedHashedPassword))

      emailVerificationToken <- emailVerificationTokenDao.insert(
        EmailVerificationToken(
          persistedUser.userId,
          systemUtilities.randomUuid(),
          persistedUser.email,
          systemUtilities.currentTime(),
          None
        )
      )

      _ <- notificationService.send(emailVerificationToken)

    } yield DatabaseUser.toUser(persistedUser)

  override def usernameExists(username: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    databaseUserDao.getByUsername(username).nonEmpty

  override def verifyEmail(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): Future[User] =
    (emailVerificationTokenDao.verifyEmail(userId, secret) ifEmpty Future.failed(
      ResourceNotFoundException("Email verification token not found")
    )).flatMap { _ =>
      withDefault(Future.failed(FatalDatabaseException)) {
        for {
          _ <- databaseUserDao.verifyEmail(userId)
          databaseUser <- databaseUserDao.getByUserId(userId)
        } yield DatabaseUser.toUser(databaseUser)
      }
    }

  override def getUserById(userId: UUID)(implicit executionContext: ExecutionContext): Future[User] =
    withDefault(Future.failed(ResourceNotFoundException(s"User not found (id = $userId)"))) {
      databaseUserDao.getByUserId(userId).map(DatabaseUser.toUser)
    }
}
