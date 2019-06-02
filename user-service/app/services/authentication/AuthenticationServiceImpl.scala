package services.authentication

import java.util.UUID

import com.ruchij.shared.utils.MonadicUtils._
import com.ruchij.shared.utils.SystemUtilities
import config.AuthenticationConfiguration
import dao.authentication.AuthenticationTokenDao
import dao.reset.ResetPasswordTokenDao
import dao.reset.models.ResetPasswordToken
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import exceptions._
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.authentication.models.AuthenticationToken
import services.crypto.CryptographyService
import services.user.models.User
import web.requests.models.{ResetPasswordRequest, UserLoginRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationServiceImpl @Inject()(
  cryptographyService: CryptographyService,
  databaseUserDao: DatabaseUserDao,
  authenticationTokenDao: AuthenticationTokenDao,
  resetPasswordTokenDao: ResetPasswordTokenDao,
  authenticationConfiguration: AuthenticationConfiguration
)(implicit systemUtilities: SystemUtilities)
    extends AuthenticationService {
  import systemUtilities.{currentTime, randomUuid}

  override def createAuthenticationToken(
    userLoginRequest: UserLoginRequest
  )(implicit executionContext: ExecutionContext): Future[AuthenticationToken] =
    userLoginRequest.username
      .map(databaseUserDao.getByUsername)
      .orElse(userLoginRequest.email.map(databaseUserDao.getByEmail))
      .fold[Future[AuthenticationToken]](Future.failed(InvalidCredentialsException)) { daoExecution =>
        for {
          databaseUser <- daoExecution ifEmpty Future.failed(InvalidCredentialsException)

          isPasswordMatch <- cryptographyService.checkPassword(userLoginRequest.password, databaseUser.password)
          _ <- predicate[Future, Throwable](isPasswordMatch, InvalidCredentialsException)

          _ <- predicate[Future, Throwable](databaseUser.emailVerified, UnverifiedEmailException(databaseUser.email))

          authenticationToken <- authenticationTokenDao.insert {
            AuthenticationToken(
              databaseUser.userId,
              randomUuid(),
              currentTime(),
              currentTime().plusMillis(authenticationConfiguration.sessionDuration.toMillis.toInt)
            )
          }
        } yield authenticationToken
      }

  override def getUserFromSessionToken(
    sessionToken: String
  )(implicit executionContext: ExecutionContext): Future[User] =
    withDefault(Future.failed(InvalidCredentialsException)) {
      for {
        authenticationToken <- authenticationTokenDao.getBySessionToken(sessionToken)
        databaseUser <- databaseUserDao.getByUserId(authenticationToken.userId)
      } yield DatabaseUser.toUser(databaseUser)
    }

  override def extendExpiryTime(
    sessionToken: String
  )(implicit executionContext: ExecutionContext): Future[AuthenticationToken] =
    withDefault(Future.failed(InvalidCredentialsException)) {
      for {
        authenticationToken <- authenticationTokenDao.getBySessionToken(sessionToken)
        updatedAuthenticationToken <- authenticationTokenDao.update(
          sessionToken,
          authenticationToken.copy(
            expiresAt = currentTime().plusMillis(authenticationConfiguration.sessionDuration.toMillis.toInt)
          )
        )
      } yield updatedAuthenticationToken
    }

  override def forgotPassword(email: String)(implicit executionContext: ExecutionContext): Future[UserId] =
    withDefault(Future.failed(ResourceNotFoundException(s"User not found (email = $email)"))) {
      databaseUserDao
        .getByEmail(email)
        .flatMapF { databaseUser =>
          resetPasswordTokenDao.insert(
            ResetPasswordToken(
              databaseUser.userId,
              randomUuid(),
              currentTime().plus(authenticationConfiguration.passwordResetTokenDuration.toMillis.toInt),
              databaseUser.email,
              currentTime(),
              None
            )
          )
        }
        .map(_.userId)
    }

  override def resetPassword(userId: UUID, resetPasswordRequest: ResetPasswordRequest)(implicit executionContext: ExecutionContext): Future[User] =
    for {
      resetPasswordToken <- resetPasswordTokenDao.getByUserIdAndSecret(userId, resetPasswordRequest.secret) ifEmpty Future.failed(InvalidCredentialsException)
      _ <- predicate[Future, Throwable](resetPasswordToken.expiresAt.isBefore(currentTime().getMillis), ExpiredPasswordResetTokenException(resetPasswordToken.expiresAt))

      databaseUser <- databaseUserDao.getByUserId(userId) ifEmpty Future.failed(ResourceNotFoundException(s"User not found (id = $userId)"))

      hashedPassword <- cryptographyService.hashPassword(resetPasswordRequest.password)

      updatedDatabaseUser <- databaseUserDao.update(databaseUser.userId, databaseUser.copy(password = hashedPassword)) ifEmpty Future.failed(FatalDatabaseException)
    }
    yield DatabaseUser.toUser(updatedDatabaseUser)
}
