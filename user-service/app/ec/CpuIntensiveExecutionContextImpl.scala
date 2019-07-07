package ec

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class CpuIntensiveExecutionContextImpl @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, CpuIntensiveExecutionContext.NAME)
    with CpuIntensiveExecutionContext
