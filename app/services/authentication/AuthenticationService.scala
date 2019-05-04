package services.authentication

import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationService {
  def authenticate()(implicit executionContext: ExecutionContext): Future[_]
}
