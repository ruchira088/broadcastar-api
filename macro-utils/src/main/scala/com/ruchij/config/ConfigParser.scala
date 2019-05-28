package com.ruchij.config

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import com.ruchij.utils.StringUtils.{camelCaseToKebabCase => kebabCase}
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.{Success, Try}

trait ConfigParser[+A] {
  def parse(config: Config, path: String): Try[A]
}

object ConfigParser {
  implicit val stringConfigParser: ConfigParser[String] = (config: Config, path: String) => Try(config.getString(path))

  implicit val finiteDurationConfigParser: ConfigParser[FiniteDuration] =
    (config: Config, path: String) =>
      Try(config.getDuration(path))
        .map(duration => FiniteDuration(duration.toMillis, TimeUnit.MILLISECONDS))

  implicit val pathConfigParser: ConfigParser[Path] =
    (config: Config, path: String) =>
      Try(config.getString(path))
        .flatMap(string => Try(Paths.get(string)))

  implicit val dateTimeParser: ConfigParser[DateTime] =
    (config: Config, path: String) => Try(config.getString(path)).flatMap(string => Try(DateTime.parse(string)))

  implicit def optionParser[A](implicit configParser: ConfigParser[A]): ConfigParser[Option[A]] =
    (config: Config, path: String) =>
      if (!config.hasPath(path)) Success(None) else configParser.parse(config, path).map(Option.apply)

  def parse[A](config: Config): Try[A] = macro parseImpl[A]

  def parseImpl[A](c: blackbox.Context)(config: c.Expr[Config])(implicit wtt: c.WeakTypeTag[A]): c.universe.Tree = {
    import c.universe._

    val parameters = wtt.tpe.companion.member(TermName("apply")).asMethod.paramLists.flatten

    val parameterValues =
      parameters.foldLeft(q"scala.util.Success(scala.collection.immutable.List.empty[Any])") {
        (params, symbol) =>
          q"""
             $params.flatMap {
              parameterList =>
                implicitly[com.ruchij.config.ConfigParser[${symbol.typeSignature}]]
                  .parse(
                    $config,
                    implicitly[com.ruchij.config.ConfigPathTransformer]
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
