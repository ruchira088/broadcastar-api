package services.notification.console.models

import services.notification.models.{NotificationConfirmation, NotificationType}

case object ConsoleNotificationConfirmation extends NotificationConfirmation[NotificationType.Console.type]
