package ec

import scala.concurrent.ExecutionContext

trait BlockingExecutionContext extends ExecutionContext

object BlockingExecutionContext {
  val NAME = "blocking-execution-context"
}
