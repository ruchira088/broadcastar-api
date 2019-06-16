package services.triggering

import akka.actor.Cancellable
import akka.stream.scaladsl.Source
import dao.user.models.DatabaseUser
import services.triggering.models.Offset

import scala.concurrent.{ExecutionContext, Future}

trait TriggeringService {
  def userCreated()(implicit executionContext: ExecutionContext): Source[DatabaseUser, Cancellable]

  def commitUserCreated(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[Offset]
}
