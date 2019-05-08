package web.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.user.UserService
import web.requests.{CreateUserRequest, RequestParser}
import web.requests.CreateUserRequest.createUserRequestValidator
import web.responses.ResponseCreator
import web.responses.models.UsernameResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(userService: UserService, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def createUser(): Action[JsValue] =
    Action.async(parse.json) {
      request =>
        ResponseCreator.create(Created) {
          for {
            createUserRequest <- Future.fromTry(RequestParser.parse[CreateUserRequest](request))
            user <- userService.createUser(createUserRequest)
          } yield user
        }
    }

  def usernameExists(username: String): Action[AnyContent] =
    Action.async {
      ResponseCreator.create(Ok) {
        for {
          exists <- userService.usernameExists(username)
        } yield UsernameResponse(username, exists)
      }
    }
}
