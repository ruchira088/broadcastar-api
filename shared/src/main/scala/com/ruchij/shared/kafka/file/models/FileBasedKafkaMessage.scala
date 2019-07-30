package com.ruchij.shared.kafka.file.models

import com.ruchij.shared.exceptions.ValidationException
import com.ruchij.shared.json.JsonUtils
import com.ruchij.shared.kafka.KafkaTopic
import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes}

import scala.util.{Failure, Try}

case class FileBasedKafkaMessage[A](kafkaTopic: KafkaTopic[A], message: A, offset: Int)

object FileBasedKafkaMessage {
  implicit def fileBasedKafkaMessageWrites[A : Writes]: Writes[FileBasedKafkaMessage[A]] =
    Json.writes[FileBasedKafkaMessage[A]]

  implicit def fileBasedKafkaMessageReads[A : Reads](implicit kafkaTopicReads: Reads[KafkaTopic[A]]): Reads[FileBasedKafkaMessage[A]] =
    Json.reads[FileBasedKafkaMessage[A]]

  def resolveTopicFromJson: PartialFunction[JsValue, Try[KafkaTopic[_]]] = {
    case jsObject @ JsObject(underlying) =>
      underlying.get("kafkaTopic")
        .fold[Try[KafkaTopic[_]]](Failure(ValidationException(s"kafkaTopic key NOT found in ${jsObject}"))) {
          json => JsonUtils.toTry { json.validate[KafkaTopic[_]] }
        }

    case _ =>
      Failure {
        ValidationException("Must be a JSON object")
      }
  }
}