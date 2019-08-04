package services.triggering

import akka.actor.Cancellable
import akka.stream.scaladsl.Source
import com.ruchij.shared.models.{EmailVerificationToken, ResetPasswordToken}
import dao.user.models.DatabaseUser
import services.triggering.models.Offset

import scala.concurrent.{ExecutionContext, Future}

trait TriggeringService {
  def userCreated()(implicit executionContext: ExecutionContext): Source[DatabaseUser, Cancellable]
  def commitUserCreated(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[Offset]

  def forgotPassword()(implicit executionContext: ExecutionContext): Source[ResetPasswordToken, Cancellable]
  def commitForgotPassword(resetPasswordToken: ResetPasswordToken)(implicit executionContext: ExecutionContext): Future[Offset]

  def emailVerification()(implicit executionContext: ExecutionContext): Source[EmailVerificationToken, Cancellable]
  def commitEmailVerification(emailVerificationToken: EmailVerificationToken)(implicit executionContext: ExecutionContext): Future[Offset]
}
