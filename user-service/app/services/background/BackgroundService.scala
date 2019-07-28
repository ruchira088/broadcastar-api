package services.background

import akka.Done
import akka.actor.Cancellable

import scala.concurrent.{ExecutionContext, Future}

trait BackgroundService {
  def start()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done])
}
