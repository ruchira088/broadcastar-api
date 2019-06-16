package com.ruchij.playground

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.javafaker.Faker
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.KafkaMessage
import com.ruchij.shared.kafka.KafkaTopic.UserCreated
import com.ruchij.shared.kafka.consumer.{KafkaConsumer, KafkaConsumerImpl}
import com.ruchij.shared.kafka.producer.{KafkaProducer, KafkaProducerImpl}
import com.ruchij.shared.models.User
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.RecordMetadata
import org.joda.time.DateTime

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

object Playground {
  private val logger: Logger = Logger[Playground.type]

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val kafkaConfiguration = KafkaConfiguration.parse(config).get

    implicit val actorSystem: ActorSystem = ActorSystem("playground")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

    val faker = Faker.instance()

    val kafkaProducer: KafkaProducer = new KafkaProducerImpl(KafkaProducerImpl.settings(kafkaConfiguration))
    val kafkaConsumer: KafkaConsumer = new KafkaConsumerImpl(KafkaConsumerImpl.settings(kafkaConfiguration))

//      Source
//        .tick(0 seconds, 100 milliseconds, (): Unit)
//        .map { _ =>
//          User(UUID.randomUUID(), DateTime.now(), faker.name().username(), faker.name().firstName(), None, faker.internet().emailAddress(), None)
//        }
//        .mapAsync(1) {
//          user =>
//            kafkaProducer.publish(KafkaMessage(user)).map(_ -> user)
//        }
//        .runWith {
//          Sink.foreach {
//            case (recordMetadata: RecordMetadata, user: User) =>
//              logger.info {
//                s"userId = ${user.userId}, offset = ${recordMetadata.offset()} "
//              }
//          }
//        }

      kafkaConsumer
        .subscribe(UserCreated)
        .mapAsync(1) {
          case (user, committableOffset) =>
            logger.info(user.toString)
            committableOffset.commitScaladsl()
        }
        .runWith(Sink.ignore)
    }
}
