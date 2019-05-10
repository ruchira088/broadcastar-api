package dao.authentication

import scalaz.OptionT
import services.authentication.models.AuthenticationToken

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationTokenDao {
  def insert(authenticationToken: AuthenticationToken)(implicit executionContext: ExecutionContext): Future[AuthenticationToken]

  def getBySessionToken(sessionToken: String)(implicit executionContext: ExecutionContext): OptionT[Future, AuthenticationToken]

  def update(sessionToken: String, authenticationToken: AuthenticationToken)(implicit executionContext: ExecutionContext): OptionT[Future, AuthenticationToken]
}
