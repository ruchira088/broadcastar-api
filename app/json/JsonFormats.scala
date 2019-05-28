package json

import java.nio.file.Path

import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object JsonFormats {
  implicit object DateTimeFormat extends Format[DateTime] {
    override def writes(dateTime: DateTime): JsValue = JsString(dateTime.toString)

    override def reads(json: JsValue): JsResult[DateTime] =
      json match {
        case JsString(string) =>
          Try(DateTime.parse(string)).fold(throwable => JsError(throwable.getMessage), dateTime => JsSuccess(dateTime))

        case _ => JsError("must be a string")
      }
  }

  implicit val finiteDurationWrites: Writes[FiniteDuration] =
    (finiteDuration: FiniteDuration) => JsString(s"${finiteDuration.length} ${finiteDuration.unit.name()}")

  implicit val pathWrites: Writes[Path] = (path: Path) => JsString(path.toAbsolutePath.toString)
}
