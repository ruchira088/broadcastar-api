package com.ruchij.shared.avro4s

import com.sksamuel.avro4s.{Decoder, Encoder, SchemaFor}
import org.apache.avro.Schema
import org.apache.avro.util.Utf8
import org.joda.time.DateTime

import scala.util.{Failure, Try}

trait AvroFormat[A] extends Decoder[A] with Encoder[A] with SchemaFor[A]

object AvroFormat {
  def unsafe[A](value: => A): A =
    Try(value)
      .recoverWith {
        case throwable: Throwable =>
          sys.error(throwable.getMessage)
          throwable.printStackTrace()

          Failure(throwable)
      }
      .get

  implicit object DateTimeFormat extends AvroFormat[DateTime] {
    override def encode(dateTime: DateTime, schema: Schema): AnyRef = new Utf8(dateTime.toString)

    override def decode(value: Any, schema: Schema): DateTime =
      unsafe { DateTime.parse(value.toString) }

    override def schema: Schema = Schema.create(Schema.Type.STRING)
  }
}
