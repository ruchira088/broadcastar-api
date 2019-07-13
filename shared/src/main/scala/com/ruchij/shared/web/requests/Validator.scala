package com.ruchij.shared.web.requests

import com.ruchij.shared.exceptions.ValidationException
import com.ruchij.shared.exceptions.aggregation.AggregatedValidationException
import com.ruchij.shared.monads.MonadicUtils._
import org.apache.commons.validator.routines.EmailValidator

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
    sequence[Try, Unit, ValidationException](validations.map(_(value)): _*)
      .flatMap {
        _.fold[Try[A]](
          validationErrors => Failure(AggregatedValidationException(validationErrors)),
          _ => Success(value)
        )
      }

  val emailValidator: String => Try[Unit] =
    email =>
      predicate[Try, Throwable](
        EmailValidator.getInstance().isValid(email),
        ValidationException(s"$email is NOT a valid email address")
    )

  val passwordValidator: String => Try[Unit] =
    password =>
      predicate[Try, Throwable](
        password.trim.length > 8,
        ValidationException("password length must be greater than 8 characters")
    )
}
