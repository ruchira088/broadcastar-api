package com.ruchij.macros.utils

import scala.util.matching.Regex

object ClassUtils {
  val classNameRegex: Regex = "(\\S+)\\$".r

  def simpleClassName(clazz: AnyRef): String =
    clazz.getClass.getSimpleName match {
      case classNameRegex(className) => className
      case className => className
    }
}
