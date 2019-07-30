package com.ruchij.shared.kafka

import com.ruchij.enum.Enum
import com.ruchij.shared.avro4s.AvroFormat.DateTimeFormat
import com.ruchij.macros.utils.ClassUtils
import com.ruchij.shared.json.JsonFormats
import com.ruchij.shared.kafka.models.VerificationEmail
import com.ruchij.shared.models.User
import com.ruchij.shared.utils.StringUtils.camelCaseToKebabCase
import com.sksamuel.avro4s.RecordFormat
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

sealed abstract class KafkaTopic[A](implicit val recordFormat: RecordFormat[A], val jsonFormat: Format[A]) extends Enum { self =>
  def name(topicPrefix: String): String =
    camelCaseToKebabCase(topicPrefix + ClassUtils.simpleClassName(self))
}

object KafkaTopic {
  implicit val userRecordFormat: RecordFormat[User] = RecordFormat[User]

  implicit case object UserCreated extends KafkaTopic[User]

  implicit case object EmailVerification extends KafkaTopic[VerificationEmail]

  val topics: Set[KafkaTopic[_]] = Enum.values[KafkaTopic[_]]

  implicit def kafkaTopicWrites[A]: Writes[KafkaTopic[A]] = (kafkaTopic: KafkaTopic[A]) => JsString(kafkaTopic.key)

  implicit val kafkaTopicReads: Reads[KafkaTopic[_]] =
    (json: JsValue) => JsonFormats.enumFormat[KafkaTopic[_]].reads(json)

  implicit def topicReads[A](implicit kafkaTopic: KafkaTopic[A]): Reads[KafkaTopic[A]] =
    (json: JsValue) =>
      kafkaTopicReads.reads(json)
        .flatMap {
          topic => if (topic.key == kafkaTopic.key) JsSuccess(kafkaTopic) else JsError(s"${topic.key} does NOT match ${kafkaTopic.key}")
        }
}
