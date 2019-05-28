package com.ruchij.enum

import scala.reflect.ClassTag

case class EnumParseException[A <: Enum](string: String, values: Set[A])(implicit classTag: ClassTag[A]) extends Exception {
  override def getMessage: String =
    s"""Unable to parse "$string" as ${classTag.runtimeClass.getSimpleName}. Possible values are (${values.map(_.key).toList.sorted.mkString(", ")})"""
}
