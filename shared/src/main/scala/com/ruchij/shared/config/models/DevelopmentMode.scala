package com.ruchij.shared.config.models

import com.ruchij.enum.Enum

sealed trait DevelopmentMode extends Enum

object DevelopmentMode {
  case object Local extends DevelopmentMode

  case object DockerCompose extends DevelopmentMode

  case object Online extends DevelopmentMode
}