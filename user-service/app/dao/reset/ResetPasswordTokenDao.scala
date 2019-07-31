package dao.reset

import java.util.UUID

import com.ruchij.shared.models.ResetPasswordToken
import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait ResetPasswordTokenDao {
  def insert(resetPasswordToken: ResetPasswordToken)(implicit executionContext: ExecutionContext): Future[ResetPasswordToken]

  def getByIndex(index: Long)(implicit executionContext: ExecutionContext): OptionT[Future, ResetPasswordToken]

  def getByUserIdAndSecret(userId: UUID, secret: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, ResetPasswordToken]
}
