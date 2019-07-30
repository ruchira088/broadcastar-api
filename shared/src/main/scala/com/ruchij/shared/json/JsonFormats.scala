package com.ruchij.shared.json

import java.nio.file.Path

import com.ruchij.enum.{Enum, EnumValues}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag
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

  def enumFormat[A <: Enum: EnumValues: ClassTag]: Format[A] = new Format[A] {
    override def writes(value: A): JsValue = JsString(value.key)

    override def reads(json: JsValue): JsResult[A] =
      json match {
        case JsString(string) =>
          Enum.parse(string).fold[JsResult[A]](throwable => JsError(throwable.getMessage), value => JsSuccess(value))
        case _ => JsError("Must be a String")
      }
  }

  implicit val finiteDurationWrites: Writes[FiniteDuration] =
    (finiteDuration: FiniteDuration) => JsString(s"${finiteDuration.length} ${finiteDuration.unit.name().toLowerCase}")

  implicit val pathWrites: Writes[Path] = (path: Path) => JsString(path.toAbsolutePath.toString)
}
