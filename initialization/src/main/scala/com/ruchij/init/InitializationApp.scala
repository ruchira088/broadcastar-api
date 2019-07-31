package com.ruchij.init

import akka.actor.ActorSystem
import com.ruchij.shared.ec.IOExecutionContextImpl
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.admin.{KafkaAdministrator, KafkaAdministratorImpl}
import com.ruchij.shared.kafka.config.{KafkaClientConfiguration, KafkaTopicConfiguration}
import com.ruchij.shared.monads.MonadicUtils
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Success

object InitializationApp {
  private val logger = Logger[InitializationApp.type]

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigFactory.load()
    implicit val actorSystem: ActorSystem = ActorSystem("initialization-app")
    implicit val ioExecutionContext: IOExecutionContextImpl = new IOExecutionContextImpl(actorSystem)

    val (confluentKafkaClientConfiguration, kafkaTopicConfiguration) =
      MonadicUtils.unsafe {
        for {
          confluentKafkaClientConfiguration <- KafkaClientConfiguration.parseConfluentConfig(config)
          kafkaTopicConfiguration <- KafkaTopicConfiguration.parse(config)
        }
        yield (confluentKafkaClientConfiguration, kafkaTopicConfiguration)

      }

    val kafkaAdministrator: KafkaAdministrator = new KafkaAdministratorImpl(confluentKafkaClientConfiguration, kafkaTopicConfiguration, ioExecutionContext)

    val result =
      Future.sequence {
        KafkaTopic.topics.map { kafkaTopic =>
          kafkaAdministrator.createTopic(kafkaTopic.name(kafkaTopicConfiguration.topicPrefix))
            .map {
              kafkaTopic.name(kafkaTopicConfiguration.topicPrefix) -> _
            }
        }
      }

    result
      .map(initializationResult => logger.info(s"initializationResult = $initializationResult"))
      .recoverWith {
        case throwable =>
          logger.error(throwable.getMessage)
          Future.failed(throwable)
      }
      .transformWith { output =>
        for {
          _ <- kafkaAdministrator.close()
          _ <- actorSystem.terminate()
          value <- Future.fromTry(output)
        } yield value
      }
      .onComplete {
        case Success(_) => sys.exit()
        case _ => sys.exit(1)
      }
  }
}
