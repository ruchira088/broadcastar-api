package services.notification.console

import javax.inject.Singleton
import services.notification.NotificationService
import services.notification.console.models.ConsoleNotificationConfirmation
import services.notification.models.NotificationType.Console
import services.notification.models.{NotificationConfirmation, NotificationSerializer, NotificationType}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ConsoleNotificationService extends NotificationService[Console.type] {

  override def send[B](message: B)(
    implicit notificationSerializer: NotificationSerializer[Console.type, B],
    executionContext: ExecutionContext
  ): Future[NotificationConfirmation[NotificationType.Console.type]] =
    Future.successful {
      println {
        notificationSerializer.serialize(message)
      }

      ConsoleNotificationConfirmation
    }
}
