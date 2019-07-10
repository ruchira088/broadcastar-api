package dao.verification

import java.util.UUID

import com.ruchij.shared.models.EmailVerificationToken
import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait EmailVerificationTokenDao {
  def insert(emailVerificationToken: EmailVerificationToken)(implicit executionContext: ExecutionContext): Future[EmailVerificationToken]

  def getByUserId(userId: UUID)(implicit executionContext: ExecutionContext): Future[List[EmailVerificationToken]]

  def verifyEmail(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationToken]
}
