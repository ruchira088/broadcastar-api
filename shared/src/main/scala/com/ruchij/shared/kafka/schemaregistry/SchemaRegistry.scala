package com.ruchij.shared.kafka.schemaregistry

import scala.concurrent.{ExecutionContext, Future}

trait SchemaRegistry {
  def removeSchema(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean]

  def listSchemas()(implicit executionContext: ExecutionContext): Future[List[String]]
}
