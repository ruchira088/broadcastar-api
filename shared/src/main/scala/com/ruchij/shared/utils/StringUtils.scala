package com.ruchij.shared.utils

object StringUtils {
  def camelCaseToKebabCase(input: String): String = camelCaseToKebabCase(input.toList, List.empty)

  private def camelCaseToKebabCase(input: List[Char], result: List[Char]): String =
    input match {
      case Nil => result.mkString
      case x :: xs if result.isEmpty => camelCaseToKebabCase(xs, x.toLower :: Nil)
      case x :: xs if x.isUpper => camelCaseToKebabCase(xs, result ::: List('-', x.toLower))
      case x :: xs => camelCaseToKebabCase(xs, result :+ x)
    }
}
