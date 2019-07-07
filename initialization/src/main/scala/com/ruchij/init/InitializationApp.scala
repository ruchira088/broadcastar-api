package com.ruchij.init

import akka.actor.ActorSystem
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.ec.IOExecutionContextImpl
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.admin.{KafkaAdministrator, KafkaAdministratorImpl}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.Success

object InitializationApp {
  private val logger = Logger[InitializationApp.type]

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("initialization-app")
    implicit val ioExecutionContext: IOExecutionContextImpl = new IOExecutionContextImpl(actorSystem)

    val kafkaConfiguration = KafkaConfiguration.parse(ConfigFactory.load()).get
    val kafkaAdministrator: KafkaAdministrator = new KafkaAdministratorImpl(kafkaConfiguration, ioExecutionContext)

    val result =
      Future.sequence {
        KafkaTopic.topics.map { kafkaTopic =>
          kafkaAdministrator.createTopic(kafkaTopic.name(kafkaConfiguration))
            .map {
              kafkaTopic.name(kafkaConfiguration) -> _
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
