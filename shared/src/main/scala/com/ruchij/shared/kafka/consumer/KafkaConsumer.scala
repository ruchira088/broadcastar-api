package com.ruchij.shared.kafka.consumer

import akka.kafka.ConsumerMessage.CommittableOffset
import akka.stream.scaladsl.Source
import com.ruchij.shared.kafka.KafkaTopic

import scala.concurrent.ExecutionContext

trait KafkaConsumer {
  def subscribe[A](kafkaTopic: KafkaTopic[A])(implicit executionContext: ExecutionContext): Source[(A, CommittableOffset), _]
}
