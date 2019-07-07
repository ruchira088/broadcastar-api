package ec

import scala.concurrent.ExecutionContext

trait CpuIntensiveExecutionContext extends ExecutionContext

object CpuIntensiveExecutionContext {
  val NAME = "cpu-intensive-execution-context"
}
