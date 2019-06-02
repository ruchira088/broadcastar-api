package services.crypto

import scala.concurrent.{ExecutionContext, Future}

trait CryptographyService {
  def hashPassword(password: String)(implicit executionContext: ExecutionContext): Future[String]

  def checkPassword(candidate: String, passwordHash: String)(implicit executionContext: ExecutionContext): Future[Boolean]
}
