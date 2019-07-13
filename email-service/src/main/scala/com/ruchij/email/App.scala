package com.ruchij.email

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.consumer.{KafkaConsumer, KafkaConsumerImpl}
import com.ruchij.shared.monads.MonadicUtils
import com.ruchij.shared.utils.SystemUtilities
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor

object App {
  def main(args: Array[String]): Unit = {
    val kafkaConfiguration =
      MonadicUtils.unsafe { KafkaConfiguration.parse(ConfigFactory.load()) }

    implicit val actorSystem: ActorSystem = ActorSystem("email-service")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

    implicit val systemUtilities: SystemUtilities = SystemUtilities

    val kafkaConsumer: KafkaConsumer = new KafkaConsumerImpl(kafkaConfiguration)

    kafkaConsumer.subscribe(KafkaTopic.EmailVerification)
      .mapAsync(1) {
        case (emailVerificationToken, committableOffset) =>
          println(emailVerificationToken)
          committableOffset.commitScaladsl()
      }
      .runWith(Sink.ignore)
  }
}
