package com.ruchij.utils

object StringUtils {

  def camelCaseToKebabCase(input: String, result: Vector[Char] = Vector.empty): String =
    input.toList match {
      case Nil => result.mkString
      case x :: xs if x.isUpper =>
        camelCaseToKebabCase(xs.mkString, if (result.isEmpty) Vector(x.toLower) else result :+ '-' :+ x.toLower)
      case x :: xs => camelCaseToKebabCase(xs.mkString, result :+ x)
    }
}
