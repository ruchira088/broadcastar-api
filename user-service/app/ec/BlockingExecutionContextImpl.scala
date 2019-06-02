package ec

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class BlockingExecutionContextImpl @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, BlockingExecutionContext.NAME)
    with BlockingExecutionContext
