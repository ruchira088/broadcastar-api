package services.notification.models

sealed trait NotificationType

object NotificationType {
  case object Email extends NotificationType

  case object Console extends NotificationType
}
