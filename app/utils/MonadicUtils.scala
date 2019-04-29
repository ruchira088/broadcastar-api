package utils

import scalaz.{Monad, MonadError, OptionT}

import scala.language.higherKinds

object MonadicUtils {
  implicit class OptionTWrapper[M[_], A](optionT: OptionT[M, A]) {
    def ifEmpty[B >: A](value: => M[B])(implicit monad: Monad[M]): M[B] =
      monad.bind[Option[A], B](optionT.run) {
        _.fold(value)(result => monad.pure(result))
      }

    def ifNotEmpty(throwable: Throwable)(implicit monadError: MonadError[M, Throwable]): M[Unit] =
      monadError.bind[Option[A], Unit](optionT.run) {
        _.fold(monadError.pure((): Unit)) { _ => monadError.raiseError(throwable) }
      }
  }
}
