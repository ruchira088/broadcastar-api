package services.notification.console.models

import services.notification.models.{NotificationConfirmation, NotificationType}

case class ConsoleNotificationConfirmation() extends NotificationConfirmation[NotificationType.Console.type]
