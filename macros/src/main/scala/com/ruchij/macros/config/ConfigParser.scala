package com.ruchij.macros.config

import com.ruchij.macros.utils.StringUtils.{camelCaseToKebabCase => kebabCase}
import com.typesafe.config.Config

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.Try

trait ConfigParser[+A] {
  def parse(config: Config, path: String): Try[A]
}

object ConfigParser {
  def parseImpl[A](c: blackbox.Context)(config: c.Expr[Config])(implicit wtt: c.WeakTypeTag[A]): c.universe.Tree = {
    import c.universe._

    val parameters = wtt.tpe.companion.member(TermName("apply")).asMethod.paramLists.flatten

    val parameterValues =
      parameters.foldLeft(q"scala.util.Success(scala.collection.immutable.List.empty[Any])") {
        (params, symbol) =>
          q"""
             $params.flatMap {
              parameterList =>
                implicitly[com.ruchij.macros.config.ConfigParser[${symbol.typeSignature}]]
                  .parse(
                    $config,
                    implicitly[com.ruchij.macros.config.ConfigPathTransformer]
                      .transform(${kebabCase(wtt.tpe.typeSymbol.name.toString)}) + "." + ${kebabCase(symbol.name.toString)}
                   )
                  .map(parameterList :+ _)
             }
           """
      }

    q"""
     $parameterValues
        .map {
          values =>
            ${wtt.tpe.typeSymbol.name.toTermName}(
              ..${parameters.indices.map {
                  index => q"values($index).asInstanceOf[${parameters(index).typeSignature}]"
                }
              }
            )
        }
      """
  }
}
