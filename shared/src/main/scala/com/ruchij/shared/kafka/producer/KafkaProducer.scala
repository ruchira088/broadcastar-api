package com.ruchij.shared.kafka.producer

import com.ruchij.shared.kafka.KafkaMessage
import org.apache.kafka.clients.producer.RecordMetadata

import scala.concurrent.{ExecutionContext, Future}

trait KafkaProducer {
  def publish[A](message: KafkaMessage[A])(implicit executionContext: ExecutionContext): Future[RecordMetadata]
}
