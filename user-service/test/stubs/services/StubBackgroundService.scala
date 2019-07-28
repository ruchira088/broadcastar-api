package stubs.services

import akka.actor.Cancellable
import javax.inject.Singleton
import services.background.BackgroundService

import scala.concurrent.ExecutionContext

@Singleton
class StubBackgroundService extends BackgroundService {
  override def start()(implicit executionContext: ExecutionContext): Cancellable = new Cancellable {
    override def cancel(): Boolean = false

    override def isCancelled: Boolean = true
  }
}
