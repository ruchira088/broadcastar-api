package services.notification.email.models

import services.notification.models.NotificationConfirmation
import services.notification.models.NotificationType.Email

case class EmailNotificationConfirmation() extends NotificationConfirmation[Email.type]
