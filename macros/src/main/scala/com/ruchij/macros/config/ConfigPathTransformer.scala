package com.ruchij.macros.config

import scala.language.implicitConversions

trait ConfigPathTransformer {
  def transform(path: String): String
}

object ConfigPathTransformer {
  implicit val defaultConfigPathTransformer: ConfigPathTransformer = (path: String) => path

  implicit def fromString(name: String): ConfigPathTransformer = (_: String) => name
}
