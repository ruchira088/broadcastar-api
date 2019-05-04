package web.requests

import exceptions.{AggregatedException, ValidationException}
import utils.MonadicUtils.{predicate, sequence, tryMonadError}

import scala.util.{Failure, Success, Try}

trait Validator[-A] {
  def validate[B <: A](value: B): Try[B]
}

object Validator {
  implicit val emptyValidator: Validator[Any] = new Validator[Any] {
    override def validate[B <: Any](value: B): Try[B] = Success(value)
  }

  def validate[A](condition: A => Boolean, validationErrorMessage: String): A => Try[Unit] =
    value => predicate[Try, Throwable](condition(value), ValidationException(validationErrorMessage))

  def combine[A](value: A)(validations: (A => Try[Unit])*): Try[A] =
    sequence(validations.map(_(value)): _*)
      .flatMap {
        _.fold[Try[A]](
          validationErrors => Failure(AggregatedException(validationErrors)),
          _ => Success(value)
        )
      }
}
