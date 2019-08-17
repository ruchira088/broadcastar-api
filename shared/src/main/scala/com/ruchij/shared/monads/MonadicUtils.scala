package com.ruchij.shared.monads

import scalaz.{Functor, Monad, MonadError, OptionT, Reader, ReaderT, Semigroup}

import scala.language.{higherKinds, reflectiveCalls}
import scala.reflect.ClassTag
import scala.util.{Failure, Try}

object MonadicUtils {
  implicit class OptionTWrapper[M[_], A](optionT: OptionT[M, A]) {
    def ifEmpty[B >: A](value: => M[B])(implicit monad: Monad[M]): M[B] =
      monad.bind[Option[A], B](optionT.run) {
        _.fold(value)(result => monad.pure(result))
      }

    def ifNotEmpty(throwable: Throwable)(implicit monadError: MonadError[M, Throwable]): M[Unit] =
      monadError.bind[Option[A], Unit](optionT.run) {
        _.fold(monadError.pure((): Unit)) { _ =>
          monadError.raiseError(throwable)
        }
      }

    def nonEmpty(implicit functor: Functor[M]): M[Boolean] = functor.map(optionT.isEmpty)(empty => !empty)
  }

  def predicate[M[_], A](condition: Boolean, failure: => A)(implicit monadError: MonadError[M, A]): M[Unit] =
    if (condition) monadError.pure((): Unit) else monadError.raiseError(failure)

  def sequence[M[_], A, B](values: M[A]*)(implicit monadError: MonadError[M, B]): M[Either[List[B], List[A]]] =
    values.toList match {
      case Nil => monadError.pure(Right(List.empty[A]))
      case x :: xs =>
        monadError.handleError(monadError.bind(x) { result =>
          monadError.map(sequence(xs: _*)) { _.map(result :: _) }
        }) { _ =>
          errorSequence(values: _*)
        }
    }

  def sequenceFailFast[M[_], A](values: M[A]*)(implicit monad: Monad[M]): M[List[A]] =
    values.headOption.fold[M[List[A]]](monad.point(List.empty)) {
      monadicValue => monad.bind(monadicValue) {
        value => monad.map(sequenceFailFast(values.tail: _*))(value :: _)
      }
    }

  private def errorSequence[M[_], A, B](
    values: M[A]*
  )(implicit monadError: MonadError[M, B]): M[Either[List[B], List[A]]] =
    values.toList match {
      case Nil => monadError.pure(Left(List.empty[B]))
      case x :: xs =>
        monadError.handleError(monadError.bind(x) { _ =>
          errorSequence(xs: _*)
        }) { error =>
          monadError.map(errorSequence(xs: _*)) { _.left.map(error :: _) }
        }
    }

  implicit def tryMonadError[Error <: Throwable: ClassTag]: MonadError[Try, Error] = new MonadError[Try, Error] {
    override def raiseError[A](error: Error): Try[A] = Failure(error)

    override def handleError[A](fa: Try[A])(f: Error => Try[A]): Try[A] =
      fa.recoverWith { case error: Error => f(error) }

    override def point[A](a: => A): Try[A] = Try(a)

    override def bind[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa.flatMap(f)
  }

  implicit def readerMonadError[Input, M[_], Error](implicit monadError: MonadError[M, Error]): MonadError[ReaderT[M, Input, ?], Error] =
    new MonadError[ReaderT[M, Input, ?], Error] {
      override def raiseError[A](error: Error): ReaderT[M, Input, A] =
        ReaderT { _ => monadError.raiseError(error) }

      override def handleError[A](fa: ReaderT[M, Input, A])(f: Error => ReaderT[M, Input, A]): ReaderT[M, Input, A] =
        ReaderT {
          input => monadError.handleError(fa(input))(error => f(error)(input))
        }

      override def bind[A, B](fa: ReaderT[M, Input, A])(f: A => ReaderT[M, Input, B]): ReaderT[M, Input, B] =
        ReaderT {
          input => monadError.bind(fa(input)) { a => f(a)(input) }
        }

      override def point[A](a: => A): ReaderT[M, Input, A] =
        ReaderT { _ => monadError.point(a) }
    }

  def withDefault[M[_]: Monad, A](default: => M[A])(value: OptionT[M, A]): M[A] = value.ifEmpty(default)

  def recoverWith[M[_], A, Error](recoveryFunction: PartialFunction[Error, A])(monad: M[A])(implicit monadError: MonadError[M, Error]): M[A] =
    monadError.handleError(monad)(recoveryFunction.andThen(value => monadError.pure(value)))

  def unsafe[M[_], A](value: => M[A])(implicit unsafeMonadCopoint: UnsafeMonadCopoint[M]): A =
    UnsafeMonadCopoint.tryUnsafeMonadCopoint
      .copoint {
        Try { unsafeMonadCopoint.copoint(value) }
          .recoverWith {
            case throwable =>
              throwable.printStackTrace()
              Failure(throwable)
          }
      }
}
