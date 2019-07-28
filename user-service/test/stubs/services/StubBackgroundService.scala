package stubs.services

import akka.Done
import akka.actor.Cancellable
import javax.inject.Singleton
import services.background.BackgroundService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StubBackgroundService extends BackgroundService {
  override def start()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
    (new Cancellable {
      override def cancel(): Boolean = false
      override def isCancelled: Boolean = true
    }, Future.successful(Done))
}
