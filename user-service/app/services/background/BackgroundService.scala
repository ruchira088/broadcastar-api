package services.background

import akka.Done

import scala.concurrent.{ExecutionContext, Future}

trait BackgroundService {
  def start()(implicit executionContext: ExecutionContext): Future[Done]
}
