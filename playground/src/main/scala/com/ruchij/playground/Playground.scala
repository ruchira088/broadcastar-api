package com.ruchij.playground

import java.nio.file.Paths
import java.util.UUID

import akka.actor.ActorSystem
import com.ruchij.shared.utils.IOUtils
import com.typesafe.scalalogging.Logger

import html.EmailVerificationTemplate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Await, Future, Promise}
import scala.language.postfixOps

object Playground {
  private val logger: Logger = Logger[Playground.type]

  def main(args: Array[String]): Unit = {
    Await.ready(
      IOUtils.writeToFile(
        Paths.get("verify-email.html"),
        EmailVerificationTemplate("Ruchira", UUID.randomUUID(), UUID.randomUUID()).body.getBytes,
        append = false
      ),
      Duration.Inf
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
