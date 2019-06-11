package com.ruchij.shared.kafka

import com.ruchij.shared.avro4s.AvroFormat.DateTimeFormat
import com.ruchij.macros.utils.ClassUtils
import com.ruchij.shared.models.User
import com.sksamuel.avro4s.RecordFormat

sealed abstract class KafkaTopic[A](implicit val recordFormat: RecordFormat[A]) { self =>
  lazy val name: String = ClassUtils.simpleClassName(self)
}

object KafkaTopic {
  implicit val userRecordFormat: RecordFormat[User] = RecordFormat[User]

  implicit case object UserCreated extends KafkaTopic[User]
}
