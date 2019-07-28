package services.background

import akka.actor.Cancellable

import scala.concurrent.ExecutionContext

trait BackgroundService {
  def start()(implicit executionContext: ExecutionContext): Cancellable
}
