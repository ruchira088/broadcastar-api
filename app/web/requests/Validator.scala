package web.requests

import scala.util.{Success, Try}

trait Validator[-A] {
  def validate[B <: A](value: B): Try[B]
}

object Validator {
  implicit val emptyValidator: Validator[Any] = new Validator[Any] {
    override def validate[B <: Any](value: B): Try[B] = Success(value)
  }
}
