package modules

import akka.actor.Cancellable
import com.typesafe.scalalogging.Logger
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import services.background.BackgroundService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class OnStartup @Inject()(backgroundService: BackgroundService, applicationLifecycle: ApplicationLifecycle)(implicit executionContext: ExecutionContext) {
  private val logger = Logger[OnStartup]

  val backgroundProcess: Cancellable = {
    println("Starting background service...")
    backgroundService.start()
  }

  applicationLifecycle.addStopHook {
    () =>
      println("Stopping background service...")
      Future.fromTry {
        Try(backgroundProcess.cancel())
      }
  }
}