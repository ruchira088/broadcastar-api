package com.ruchij.email

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.ruchij.email.config.EmailConfiguration
import com.ruchij.email.services.email.client.{EmailClient, StubEmailClient}
import com.ruchij.email.services.email.{EmailParser, EmailSerializer}
import com.ruchij.shared.ec.{IOExecutionContext, IOExecutionContextImpl}
import com.ruchij.shared.json.JsonUtils.prettyPrintJson
import com.ruchij.shared.kafka.KafkaTopic
import com.ruchij.shared.kafka.config.{FileBasedKafkaClientConfiguration, KafkaClientConfiguration, KafkaTopicConfiguration}
import com.ruchij.shared.kafka.consumer.{KafkaConsumer, KafkaConsumerImpl}
import com.ruchij.shared.kafka.file.FileBasedKafkaBroker
import com.ruchij.shared.monads.MonadicUtils
import com.ruchij.shared.utils.SystemUtilities
import com.sendgrid.SendGrid
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import scalaz.std.scalaFuture.futureInstance

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.language.postfixOps
import scala.util.Success

object App {
  private val logger = Logger[App.type]

  def main(args: Array[String]): Unit =
    Await.ready(run(), Duration.Inf)

  def run(): Future[Done] = {
    implicit val actorSystem: ActorSystem = ActorSystem("email-service")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

    implicit val systemUtilities: SystemUtilities = SystemUtilities
    val config = ConfigFactory.load()

    val (emailConfiguration, kafkaClientConfiguration, kafkaTopicConfiguration, fileBasedKafkaClientConfiguration) =
      MonadicUtils.unsafe {
        for {
          emailConfiguration <- EmailConfiguration.parse(config)
          kafkaClientConfiguration <- KafkaClientConfiguration.parseLocalConfig(config)
          kafkaTopicConfiguration <- KafkaTopicConfiguration.parse(config)
          fileBasedKafkaClientConfiguration <- FileBasedKafkaClientConfiguration.parse(config)
        }
        yield (emailConfiguration, kafkaClientConfiguration, kafkaTopicConfiguration, fileBasedKafkaClientConfiguration)
      }

    val ioExecutionContext: IOExecutionContext = new IOExecutionContextImpl(actorSystem)
    val sendGrid = new SendGrid(emailConfiguration.sendGridApiKey.value)

    val dependencies = Dependencies(sendGrid, ioExecutionContext)

    println { prettyPrintJson(emailConfiguration) }
    println { prettyPrintJson(kafkaClientConfiguration) }
    println { prettyPrintJson(kafkaTopicConfiguration) }

    val kafkaConsumer: KafkaConsumer = new KafkaConsumerImpl(kafkaClientConfiguration, kafkaTopicConfiguration)
//  val kafkaConsumer = new FileBasedKafkaBroker(fileBasedKafkaClientConfiguration)

    execute(KafkaTopic.EmailVerification)(dependencies, kafkaConsumer, StubEmailClient)
  }

  def execute[Message, EmailBody, ClientMessage](
    kafkaTopic: KafkaTopic[Message]
  )(dependencies: Dependencies, kafkaConsumer: KafkaConsumer, emailClient: EmailClient[ClientMessage, _])(
    implicit emailParser: EmailParser[Message, EmailBody],
    emailSerializer: EmailSerializer[EmailBody, ClientMessage],
    materializer: ActorMaterializer,
    executionContext: ExecutionContext
  ): Future[Done] =
    kafkaConsumer
      .subscribe(kafkaTopic)
      .mapAsync(parallelism = 1) {
        case (message, committableOffset) =>
          Future.fromTry { emailParser.email(message) }
            .andThen {
              case Success(email) =>
                logger.info(s"Sending email to ${email.to}")
            }
            .map(_ -> committableOffset)
      }
      .mapAsync(parallelism = 1) {
        case (email, committableOffset) =>
          emailClient
            .send(email)
            .flatMapK { response =>
              committableOffset.commitScaladsl()
            }
            .run(emailClient.local(dependencies))
      }
      .runWith(Sink.ignore)
      .recoverWith {
        case throwable =>
          logger.error(throwable.getMessage, throwable)
          execute(kafkaTopic)(dependencies, kafkaConsumer, emailClient)
      }
}
