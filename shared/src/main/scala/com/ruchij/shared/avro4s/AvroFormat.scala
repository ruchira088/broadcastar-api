package com.ruchij.shared.avro4s

import com.ruchij.shared.monads.MonadicUtils
import com.sksamuel.avro4s.{Decoder, Encoder, SchemaFor}
import org.apache.avro.Schema
import org.apache.avro.util.Utf8
import org.joda.time.DateTime

import scala.util.Try

trait AvroFormat[A] extends Decoder[A] with Encoder[A] with SchemaFor[A]

object AvroFormat {
  implicit object DateTimeFormat extends AvroFormat[DateTime] {
    override def encode(dateTime: DateTime, schema: Schema): AnyRef = new Utf8(dateTime.toString)

    override def decode(value: Any, schema: Schema): DateTime =
      MonadicUtils.unsafe { Try(DateTime.parse(value.toString)) }

    override def schema: Schema = Schema.create(Schema.Type.STRING)
  }
}
