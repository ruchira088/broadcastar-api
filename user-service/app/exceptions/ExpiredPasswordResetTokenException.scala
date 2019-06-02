package exceptions

import org.joda.time.DateTime

case class ExpiredPasswordResetTokenException(expiredAt: DateTime) extends Exception {
  override def getMessage: String = s"Password reset token expired at ${expiredAt.toString}"
}
