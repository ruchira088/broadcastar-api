package com.ruchij.shared.kafka.admin

import scala.concurrent.{ExecutionContext, Future}

trait KafkaAdministrator {
  def createTopic(topicName: String)(
    implicit executionContext: ExecutionContext
  ): Future[Boolean]

  def close()(implicit executionContext: ExecutionContext): Future[Unit]
}
