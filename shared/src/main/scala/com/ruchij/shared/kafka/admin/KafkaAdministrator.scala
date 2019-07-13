package com.ruchij.shared.kafka.admin

import scala.concurrent.{ExecutionContext, Future}

trait KafkaAdministrator {
  def createTopic(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean]

  def deleteTopic(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean]

  def listTopics()(implicit executionContext: ExecutionContext): Future[Set[String]]

  def close()(implicit executionContext: ExecutionContext): Future[Unit]
}
