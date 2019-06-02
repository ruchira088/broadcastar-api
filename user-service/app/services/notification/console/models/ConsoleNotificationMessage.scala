package services.notification.console.models

import dao.verification.models.EmailVerificationToken
import com.ruchij.shared.json.JsonFormats.DateTimeFormat
import play.api.libs.json.{Json, OWrites, Writes}
import services.notification.models.NotificationType.Console
import services.notification.models.{NotificationMessage, NotificationSerializer}

case class ConsoleNotificationMessage(message: String) extends NotificationMessage[Console.type]

object ConsoleNotificationMessage {
  implicit val emailVerificationTokenWrites: OWrites[EmailVerificationToken] = Json.writes[EmailVerificationToken]

  implicit def consoleNotificationSerializer[A](implicit writes: Writes[A]): NotificationSerializer[Console.type, A] =
    (value: A) => ConsoleNotificationMessage { Json.prettyPrint(writes.writes(value)) }
}
