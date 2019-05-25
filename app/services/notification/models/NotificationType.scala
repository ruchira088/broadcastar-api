package services.notification.models

import services.notification.email.models.EmailNotification

sealed trait NotificationType { self =>
  type Notification <: NotificationMessage[self.type]
}

object NotificationType {
  case object Email extends NotificationType {
    override type Notification = EmailNotification
  }

  case object Console extends NotificationType {

  }
}
