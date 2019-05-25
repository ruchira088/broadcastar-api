package services.notification.models

trait NotificationSerializer[A <: NotificationType, -B] {
  def serialize(value: B): NotificationMessage[A]
}
