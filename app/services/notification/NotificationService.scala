package services.notification

import services.notification.models.{NotificationConfirmation, NotificationSerializer, NotificationType}

import scala.concurrent.{ExecutionContext, Future}

trait NotificationService[A <: NotificationType] {

  def send[B](message: B)(
    implicit notificationSerializer: NotificationSerializer[A, B],
    executionContext: ExecutionContext
  ): Future[NotificationConfirmation[A]]
}
