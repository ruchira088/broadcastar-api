package services.authentication

import config.AuthenticationConfiguration
import dao.authentication.AuthenticationTokenDao
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import exceptions.{InvalidCredentialsException, UnverifiedEmailException}
import javax.inject.{Inject, Singleton}
import scalaz.std.scalaFuture.futureInstance
import services.authentication.models.AuthenticationToken
import services.crypto.CryptographyService
import services.user.models.User
import utils.MonadicUtils.{OptionTWrapper, predicate, withDefault}
import utils.SystemUtilities
import web.requests.UserLoginRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthenticationServiceImpl @Inject()(
  cryptographyService: CryptographyService,
  databaseUserDao: DatabaseUserDao,
  authenticationTokenDao: AuthenticationTokenDao,
  authenticationConfiguration: AuthenticationConfiguration
)(implicit systemUtilities: SystemUtilities)
    extends AuthenticationService {
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
              systemUtilities.randomUuid(),
              systemUtilities.currentTime(),
              systemUtilities.currentTime().plusMillis(authenticationConfiguration.sessionDuration.toMillis.toInt)
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
        databaseUser <- databaseUserDao.getById(authenticationToken.userId)
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
            expiresAt =
              systemUtilities.currentTime().plusMillis(authenticationConfiguration.sessionDuration.toMillis.toInt)
          )
        )
      } yield updatedAuthenticationToken
    }
}
