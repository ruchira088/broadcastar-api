package com.ruchij.enum

import com.ruchij.macros.utils.ClassUtils

import scala.language.experimental.macros
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait Enum {
  self =>

  def key: String = ClassUtils.simpleClassName(self)
}

object Enum {
  def values[A <: Enum](implicit enumValues: EnumValues[A]): Set[A] = enumValues.values

  def parse[A <: Enum : ClassTag](value: String)(implicit enumValues: EnumValues[A]): Try[A] =
    enumValues.values
      .find {
        enum => enum.key.equalsIgnoreCase(value)
      }
      .fold[Try[A]]{ Failure(EnumParseException[A](value, enumValues.values)) }(Success.apply)
}
