package com.ruchij.playground

import java.nio.file.Paths

import akka.actor.ActorSystem
import com.github.javafaker.Faker
import com.ruchij.shared.utils.IOUtils
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object Playground {
  private val logger: Logger = Logger[Playground.type]

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("playground")

    logger.info("Application started.")

    IOUtils.lockFile(
      Paths.get("file.lock"),
      waitFor(30 seconds).flatMap(
        _ => IOUtils.writeToFile(Paths.get("file.lock"), s"${Faker.instance().name().username()}\n".getBytes, append = true)
      )
    )
  }

  def waitFor(finiteDuration: FiniteDuration)(implicit actorSystem: ActorSystem): Future[Unit] = {
    val promise = Promise[Unit]

    logger.info(s"Waiting for $finiteDuration...")

    actorSystem.scheduler.scheduleOnce(finiteDuration) {
      promise.success((): Unit)

      logger.info("Waiting completed.")
    }

    promise.future
  }
}
