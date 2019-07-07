package com.ruchij.enum

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait EnumValues[A <: Enum] {
  val values: Set[A]
}

object EnumValues {
  implicit def apply[A <: Enum]: EnumValues[A] = macro applyImpl[A]

  def applyImpl[A <: Enum](c: blackbox.Context)(implicit wtt: c.WeakTypeTag[A]): c.universe.Tree = {
    import c.universe._

    val enumValues =
      wtt.tpe.typeSymbol.asClass.knownDirectSubclasses
        .map {
          symbol => q"${c.mirror.staticModule(symbol.fullName)}"
        }

    q"""
       new com.ruchij.enum.EnumValues[${wtt.tpe}] {
          val values: Set[${wtt.tpe}] = Set(..$enumValues)
       }
     """
  }
}
