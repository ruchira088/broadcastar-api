package com.ruchij.shared.kafka

import com.ruchij.enum.Enum
import com.ruchij.shared.avro4s.AvroFormat.DateTimeFormat
import com.ruchij.macros.utils.ClassUtils
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.models.User
import com.ruchij.shared.utils.StringUtils.camelCaseToKebabCase
import com.sksamuel.avro4s.RecordFormat

sealed abstract class KafkaTopic[A](implicit val recordFormat: RecordFormat[A]) extends Enum { self =>
  def name(topicPrefix: String): String =
    camelCaseToKebabCase(topicPrefix + ClassUtils.simpleClassName(self))
}

object KafkaTopic {
  implicit val userRecordFormat: RecordFormat[User] = RecordFormat[User]

  implicit case object UserCreated extends KafkaTopic[User]

  implicit case object EmailVerification extends KafkaTopic[VerificationEmail]

  val topics: Set[KafkaTopic[_]] = Enum.values[KafkaTopic[_]]
}
