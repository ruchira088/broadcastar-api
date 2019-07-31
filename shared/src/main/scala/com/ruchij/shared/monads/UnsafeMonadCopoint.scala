package com.ruchij.shared.monads

import scala.language.higherKinds
import scala.util.Try

trait UnsafeMonadCopoint[M[_]] {
  def copoint[A](value: => M[A]): A
}

object UnsafeMonadCopoint {
  implicit val tryUnsafeMonadCopoint: UnsafeMonadCopoint[Try] =
    new UnsafeMonadCopoint[Try] {
      override def copoint[A](value: => Try[A]): A = value.get
    }

  implicit val optionUnsafeMonadCopoint: UnsafeMonadCopoint[Option] =
    new UnsafeMonadCopoint[Option] {
      override def copoint[A](value: => Option[A]): A = value.get
    }
}
