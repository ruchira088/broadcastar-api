package web.controllers

import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.authentication.AuthenticationService
import services.user.UserService
import web.actions.user.UserAction
import web.requests.{CreateUserRequest, EmailVerificationRequest, RequestParser, UserLoginRequest}
import web.requests.CreateUserRequest.createUserRequestValidator
import web.requests.UserLoginRequest.userLoginRequestValidator
import web.responses.ResponseCreator
import web.responses.models.{SessionTokenResponse, UsernameResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(userService: UserService, authenticationService: AuthenticationService, userAction: UserAction, controllerComponents: ControllerComponents)(implicit executionContext: ExecutionContext)
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

  def sessionToken(): Action[JsValue] =
    Action.async(parse.json) {
      request =>
        ResponseCreator.create(Created) {
          for {
            userLoginRequest <- Future.fromTry(RequestParser.parse[UserLoginRequest](request))
            authenticationToken <- authenticationService.createAuthenticationToken(userLoginRequest)
          }
          yield SessionTokenResponse.fromAuthenticationToken(authenticationToken)
        }
    }

  def verifyEmail(userId: UUID): Action[JsValue] =
    userAction.forId(userId)
      .async(parse.json) {
        request =>
          ResponseCreator.create(Ok) {
            for {
              emailVerificationRequest <- Future.fromTry(RequestParser.parse[EmailVerificationRequest](request))
              user <- userService.verifyEmail(userId, emailVerificationRequest.verificationToken)
            }
            yield user
          }
  }
}
