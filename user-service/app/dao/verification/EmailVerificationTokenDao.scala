package dao.verification

import java.util.UUID

import dao.verification.models.EmailVerificationToken
import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait EmailVerificationTokenDao {
  def insert(emailVerificationToken: EmailVerificationToken)(implicit executionContext: ExecutionContext): Future[EmailVerificationToken]

  def getByUserIdAndSecret(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationToken]

  def verifyEmail(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationToken]
}
