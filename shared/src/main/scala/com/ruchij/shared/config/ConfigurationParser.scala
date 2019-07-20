package com.ruchij.shared.config

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

import com.ruchij.macros.config.ConfigParser
import com.ruchij.macros.config.ConfigParser.parseImpl
import com.ruchij.shared.config.models.Secret
import com.typesafe.config.Config
import org.joda.time.DateTime

import scala.concurrent.duration.FiniteDuration
import scala.language.experimental.macros
import scala.util.{Success, Try}

object ConfigurationParser {
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

  implicit val intParser: ConfigParser[Int] = (config: Config, path: String) => Try(config.getInt(path))

  implicit def secretParser[A](implicit configParser: ConfigParser[A]): ConfigParser[Secret[A]] =
    (config: Config, path: String) => configParser.parse(config, path).map(Secret.apply)

  implicit def optionParser[A](implicit configParser: ConfigParser[A]): ConfigParser[Option[A]] =
    (config: Config, path: String) =>
      if (!config.hasPath(path)) Success(None) else configParser.parse(config, path).map(Option.apply)

  def parse[A](config: Config): Try[A] = macro parseImpl[A]
}
