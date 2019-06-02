package web.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}

@Singleton
class MessageController @Inject()(controllerComponents: ControllerComponents)
    extends AbstractController(controllerComponents) {

}
