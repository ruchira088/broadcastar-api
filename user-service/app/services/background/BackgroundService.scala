package services.background

import akka.Done
import akka.actor.Cancellable

import scala.concurrent.{ExecutionContext, Future}

trait BackgroundService {
  def start()(implicit executionContext: ExecutionContext): (Cancellable, Future[Done])
}

object BackgroundService {
  val CANCELLABLE: Cancellable = new Cancellable {
    override def cancel(): Boolean = true
    override def isCancelled: Boolean = false
  }

  def combineCancellable(cancellableA: Cancellable, cancellableB: Cancellable): Cancellable =
    new Cancellable {
      override def cancel(): Boolean = {
        val resultA = cancellableA.cancel()
        val resultB = cancellableB.cancel()

        resultA && resultB
      }

      override def isCancelled: Boolean = cancellableA.isCancelled && cancellableB.isCancelled
    }

  def combine(values: List[(Cancellable, Future[Done])])(implicit executionContext: ExecutionContext): (Cancellable, Future[Done]) =
    values.foldLeft(CANCELLABLE, Future.successful(Done)) {
      case ((accCancellable, accDone), (cancellable, done)) =>
        (combineCancellable(accCancellable, cancellable), Future.sequence(List(accDone, done)).map(_ => Done))
    }
}