package web.controllers

import java.util.UUID

import com.ruchij.shared.web.requests.RequestParser
import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.authentication.AuthenticationService
import services.user.UserService
import web.actions.authenticated.{AuthenticatedAction, AuthenticatedRequest}
import web.actions.user.UserAction
import web.requests.models.CreateUserRequest.createUserRequestValidator
import web.requests.models.ForgotPasswordRequest.forgotPasswordRequestValidator
import web.requests.models.ResetPasswordRequest.resetPasswordRequestValidator
import web.requests.models.UserLoginRequest.userLoginRequestValidator
import web.requests.models._
import web.responses.ResponseCreator
import web.responses.models.{ForgotPasswordResponse, ResendVerificationEmailResponse, SessionTokenResponse, UsernameResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(
  userService: UserService,
  authenticationService: AuthenticationService,
  userAction: UserAction,
  authenticatedAction: AuthenticatedAction,
  controllerComponents: ControllerComponents
)(implicit executionContext: ExecutionContext)
    extends AbstractController(controllerComponents) {

  def createUser(): Action[JsValue] =
    Action.async(parse.json) { request =>
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
    Action.async(parse.json) { request =>
      ResponseCreator.create(Created) {
        for {
          userLoginRequest <- Future.fromTry(RequestParser.parse[UserLoginRequest](request))
          authenticationToken <- authenticationService.createAuthenticationToken(userLoginRequest)
        } yield SessionTokenResponse.fromAuthenticationToken(authenticationToken)
      }
    }

  def verifyEmail(userId: UUID): Action[JsValue] =
    userAction
      .forId(userId)
      .async(parse.json) { request =>
        ResponseCreator.create(Ok) {
          for {
            EmailVerificationRequest(verificationToken) <- Future.fromTry(
              RequestParser.parse[EmailVerificationRequest](request)
            )
            user <- userService.verifyEmail(userId, verificationToken)
          } yield user
        }
      }

  def resendVerificationEmail(): Action[JsValue] =
    Action.async(parse.json) { request =>
      ResponseCreator.create(Ok) {
        for {
          resendVerificationEmailRequest <- Future.fromTry(RequestParser.parse[ResendVerificationEmailRequest](request))
          emailVerificationToken <- userService.resendVerificationEmail(resendVerificationEmailRequest.email)
        }
        yield ResendVerificationEmailResponse(emailVerificationToken.email)
      }
    }

  def forgotPassword(): Action[JsValue] =
    Action.async(parse.json) { request =>
      ResponseCreator.create(Created) {
        for {
          ForgotPasswordRequest(email) <- Future.fromTry(RequestParser.parse[ForgotPasswordRequest](request))
          resetPasswordToken <- authenticationService.forgotPassword(email)
        } yield ForgotPasswordResponse(resetPasswordToken.email)
      }
    }

  def resetPassword(userId: UUID): Action[JsValue] =
    userAction
      .forId(userId)
      .async(parse.json) { request =>
        ResponseCreator.create(Ok) {
          for {
            resetPasswordRequest <- Future.fromTry(RequestParser.parse[ResetPasswordRequest](request))
            user <- authenticationService.resetPassword(userId, resetPasswordRequest)
          } yield user
        }
      }

  def authenticatedUser(): Action[Unit] =
    authenticatedAction.async(parse.empty) {
      case AuthenticatedRequest(user, _) => ResponseCreator.create(Ok) { Future.successful(user) }
    }
}
