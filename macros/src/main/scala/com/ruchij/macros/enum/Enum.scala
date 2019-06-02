package com.ruchij.enum

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

trait Enum {
  self =>

  def key: String = Enum.simpleClassName(self)
}

object Enum {
  val ClassNameRegex: Regex = "(\\S+)\\$".r

  def simpleClassName(clazz: AnyRef): String =
    clazz.getClass.getSimpleName match {
      case ClassNameRegex(className) => className
      case className => className
    }

  def values[A <: Enum](implicit enumValues: EnumValues[A]): Set[A] = enumValues.values

  def parse[A <: Enum : ClassTag](value: String)(implicit enumValues: EnumValues[A]): Try[A] =
    enumValues.values
      .find {
        enum => enum.key.equalsIgnoreCase(value)
      }
      .fold[Try[A]]{ Failure(EnumParseException[A](value, enumValues.values)) }(Success.apply)
}
