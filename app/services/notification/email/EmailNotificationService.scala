package services.notification.email

import javax.inject.{Inject, Singleton}
import services.notification.NotificationService
import services.notification.email.models.EmailNotificationConfirmation
import services.notification.models.{NotificationSerializer, NotificationType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailNotificationService @Inject()() extends NotificationService[NotificationType.Email.type] {

  override def send[B](message: B)(
    implicit notificationSerializer: NotificationSerializer[NotificationType.Email.type, B],
    executionContext: ExecutionContext
  ): Future[EmailNotificationConfirmation] = ???
}
