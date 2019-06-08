package com.ruchij.shared.web.actions

import com.ruchij.shared.config.AuthenticationConfiguration
import javax.inject.Inject
import play.api.mvc.{ActionBuilderImpl, BodyParsers}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

class AuthenticatedAction @Inject()(
  ws: WSClient,
  authenticatedConfiguration: AuthenticationConfiguration,
  parser: BodyParsers.Default
)(implicit executionContext: ExecutionContext)
    extends ActionBuilderImpl(parser) {}
