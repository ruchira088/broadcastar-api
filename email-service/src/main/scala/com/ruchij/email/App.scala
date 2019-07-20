package com.ruchij.email

import java.nio.file.Paths

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.ruchij.email.config.EmailConfiguration
import com.ruchij.email.services.email.client.SendGridEmailClient
import com.ruchij.email.services.email.models.Email
import com.ruchij.shared.kafka.stubs.StubKafkaBroker
import com.ruchij.shared.kafka.stubs.StubKafkaBroker.verificationEmailGenerator
import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.ec.{IOExecutionContext, IOExecutionContextImpl}
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.consumer.{KafkaConsumer, KafkaConsumerImpl}
import com.ruchij.shared.monads.MonadicUtils
import com.ruchij.shared.utils.{IOUtils, SystemUtilities}
import com.sendgrid.SendGrid
import com.typesafe.config.ConfigFactory
import scalaz.std.scalaFuture.futureInstance

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future, Promise}
import scala.language.postfixOps

object App {
  def main(args: Array[String]): Unit =
    Await.ready(Future.successful(run()), Duration.Inf)

  def render(): Future[Integer] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val systemUtilities: SystemUtilities = SystemUtilities

    IOUtils.writeToFile(
      Paths.get("email-template.html"),
      Email.create(StubKafkaBroker.verificationEmailGenerator.generate()).content.body.getBytes
    )
  }

  def run(): Cancellable = {
    implicit val actorSystem: ActorSystem = ActorSystem("email-service")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

    implicit val systemUtilities: SystemUtilities = SystemUtilities

    val emailConfiguration =
      MonadicUtils.unsafe { EmailConfiguration.parse(ConfigFactory.load()) }

    val ioExecutionContext: IOExecutionContext = new IOExecutionContextImpl(actorSystem)
    val sendGrid = new SendGrid(emailConfiguration.sendGridApiKey)

    val dependencies = Dependencies(sendGrid, ioExecutionContext)
    val emailClient = SendGridEmailClient

//    val kafkaConfiguration =
//      MonadicUtils.unsafe { KafkaConfiguration.parse(ConfigFactory.load()) }
//
//    val kafkaConsumer: KafkaConsumer = new KafkaConsumerImpl(kafkaConfiguration)
    val kafkaConsumer = new StubKafkaBroker()

    kafkaConsumer
      .subscribe(KafkaTopic.EmailVerification)
      .mapAsync(parallelism = 1) {
        case (verificationEmail, committableOffset) =>
          emailClient.send(Email.create(verificationEmail))
              .flatMapK {
                response =>
                  println(response.getStatusCode)
                  committableOffset.commitScaladsl()
              }
              .local(emailClient.local)
              .run(dependencies)

      }
      .runWith(Sink.ignore)

    StubKafkaBroker.publishGeneratedMessages(KafkaTopic.EmailVerification, kafkaConsumer, interval = 30 seconds)
  }

  def delay(
    finiteDuration: FiniteDuration
  )(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Future[FiniteDuration] = {
    val promise = Promise[FiniteDuration]

    actorSystem.scheduler.scheduleOnce(finiteDuration) {
      promise.success(finiteDuration)
    }

    promise.future
  }
}
