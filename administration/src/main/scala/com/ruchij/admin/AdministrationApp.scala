package com.ruchij.admin

import akka.actor.{ActorSystem, Terminated}
import akka.stream.ActorMaterializer
import com.ruchij.shared.ec.IOExecutionContext
import com.ruchij.shared.kafka.admin.KafkaAdministratorImpl
import com.ruchij.shared.kafka.config.{KafkaClientConfiguration, KafkaTopicConfiguration}
import com.ruchij.shared.kafka.schemaregistry.SchemaRegistryImpl
import com.ruchij.shared.monads.MonadicUtils.unsafe
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object AdministrationApp {
  private val logger = Logger[AdministrationApp.type]

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global

    Await.ready(removeTopicsAndSchemas, 30 seconds)
  }

  def removeTopicsAndSchemas(implicit executionContext: ExecutionContext): Future[Terminated] = {
    val configuration = ConfigFactory.load()
    val confluentKafkaClientConfiguration = unsafe(KafkaClientConfiguration.parseConfluentConfig(configuration))
    val topicConfiguration = unsafe(KafkaTopicConfiguration.parse(configuration))

    implicit val actorSystem: ActorSystem = ActorSystem("administrator-app")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    val ahcWsClient = AhcWSClient()

    val kafkaAdministrator =
      new KafkaAdministratorImpl(confluentKafkaClientConfiguration, topicConfiguration, ioExecutionContext)
    val schemaRegistry = new SchemaRegistryImpl(ahcWsClient, confluentKafkaClientConfiguration)

    val task =
      for {
        topics <- kafkaAdministrator.listTopics().map(_.toList)
        topicDeletions <- Future.sequence(topics.map(kafkaAdministrator.deleteTopic))

        schemas <- schemaRegistry.listSchemas()
        schemaDeletions <- Future.sequence(schemas.map(schemaRegistry.removeSchema))

        _ <- kafkaAdministrator.close()
      } yield (topics, topicDeletions, schemas, schemaDeletions)

    task
      .andThen {
        case Success((topics, topicDeletions, schemas, schemaDeletions)) =>
          val topicResults =
            topics.zip(topicDeletions).map { case (topic, result) => s"$topic -> $result" }

          val schemaResults =
            schemas.zip(schemaDeletions).map { case (schema, result) => s"$schema -> $result" }

          logger.info(s"Topic results: ${topicResults.mkString(", ")}")
          logger.info(s"Schema results: ${schemaResults.mkString(", ")}")

        case Failure(exception) => logger.error(exception.getMessage, exception)
      }
      .flatMap { _ =>
        ahcWsClient.close()
        actorMaterializer.shutdown()
        actorSystem.terminate()
      }
  }

  def ioExecutionContext(implicit executionContext: ExecutionContext): IOExecutionContext =
    new IOExecutionContext {
      override def execute(runnable: Runnable): Unit = executionContext.execute(runnable)
      override def reportFailure(cause: Throwable): Unit = executionContext.reportFailure(cause)
    }
}
