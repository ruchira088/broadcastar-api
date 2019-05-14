package dao.verification

import java.util.UUID

import dao.verification.models.EmailVerificationEntry
import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait EmailVerificationEntryDao {
  def insert(emailVerificationEntry: EmailVerificationEntry)(implicit executionContext: ExecutionContext): Future[EmailVerificationEntry]

  def getByUserIdAndVerificationToken(userId: UUID, verificationToken: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationEntry]

  def verifyEmail(userId: UUID, verificationToken: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationEntry]
}
