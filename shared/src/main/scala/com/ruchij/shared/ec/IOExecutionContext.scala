package com.ruchij.shared.ec

import scala.concurrent.ExecutionContext

trait IOExecutionContext extends ExecutionContext

object IOExecutionContext {
  val NAME = "io-execution-context"
}
