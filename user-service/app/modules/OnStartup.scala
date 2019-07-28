package modules

import akka.Done
import akka.actor.Cancellable
import com.typesafe.scalalogging.Logger
import javax.inject.{Inject, Singleton}
import play.api.inject.ApplicationLifecycle
import services.background.BackgroundService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class OnStartup @Inject()(backgroundService: BackgroundService, applicationLifecycle: ApplicationLifecycle)(implicit executionContext: ExecutionContext) {
  private val logger = Logger[OnStartup]

  var cancellable: Option[Cancellable] = None

  def start(): Future[Done] = {
    logger.info("Starting background service...")

    val (cancel, process) = backgroundService.start()

    logger.info("Background service started.")

    cancellable = Some(cancel)

    process.recoverWith {
      case throwable =>
        logger.error(throwable.getMessage, throwable)

        Future.fromTry(Try(cancel.cancel()))
          .transformWith {
            _ => start()
          }
    }
  }

  applicationLifecycle.addStopHook {
    () =>
      Future.fromTry {
        Try {
          cancellable.foreach {
            cancel =>
              logger.info("Stopping background service...")
              cancel.cancel()
          }
        }
      }
  }

  start().onComplete {
    case Success(_) => logger.info("Background service was successfully shutdown.")
    case Failure(throwable) => logger.error(throwable.getMessage, throwable)
  }
}