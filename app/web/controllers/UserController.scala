package web.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import web.requests.{CreateUserRequest, RequestParser}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {
  def create(): Action[JsValue] =
    Action.async(parse.json) {
      request =>
        for {
          createUserRequest <- Future.fromTry(RequestParser.parse[CreateUserRequest](request))
        } yield ???
    }
}
