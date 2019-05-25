package services.notification.email.models

import dao.verification.models.EmailVerificationToken
import services.notification.models.NotificationType.Email
import services.notification.models.{NotificationMessage, NotificationSerializer}

case class EmailNotification(email: String, title: String) extends NotificationMessage[Email.type]

object EmailNotification {
  implicit val emailVerificationTokenNotificationSerializer: NotificationSerializer[Email.type, EmailVerificationToken] =
    (emailVerificationToken: EmailVerificationToken) => EmailNotification(emailVerificationToken.email, "")
}
