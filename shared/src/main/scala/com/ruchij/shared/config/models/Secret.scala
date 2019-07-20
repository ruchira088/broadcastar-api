package com.ruchij.shared.config.models

import play.api.libs.json.{JsString, Writes}

case class Secret[+A](value: A)

object Secret {
  implicit def secretWrites[A]: Writes[Secret[A]] = (secret: Secret[A]) => JsString(mask(secret.value.toString))

  def mask(input: String): String = mask(input.toList, List.empty)

  private def mask(input: List[Char], accumulator: List[Char]): String =
    (input, accumulator) match {
      case (Nil, result) => result.mkString
      case (x :: xs, result) if input.length > 12 && accumulator.length < 4 => mask(xs, result :+ x)
      case (x :: xs, result) if input.length < 5 && accumulator.length > 11 => mask(xs, result :+ x)
      case (_ :: xs, result) => mask(xs, result :+ '*')
    }
}
