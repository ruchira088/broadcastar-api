package stubs.services

import akka.Done
import javax.inject.Singleton
import services.background.BackgroundService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StubBackgroundService extends BackgroundService {
  override def sendNewUsersToKafka()(implicit executionContext: ExecutionContext): Future[Done] =
    Future.successful(Done)
}
