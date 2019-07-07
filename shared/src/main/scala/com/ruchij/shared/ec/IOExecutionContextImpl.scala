package com.ruchij.shared.ec

import akka.actor.ActorSystem
import javax.inject.Inject
import play.api.libs.concurrent.CustomExecutionContext

class IOExecutionContextImpl @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, IOExecutionContext.NAME)
    with IOExecutionContext
