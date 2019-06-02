package web.actions.user

import java.util.UUID

import com.ruchij.shared.utils.MonadicUtils.recoverWith
import javax.inject.Inject
import play.api.mvc._
import scalaz.std.scalaFuture.futureInstance
import services.user.UserService
import web.responses.ResponseCreator

import scala.concurrent.{ExecutionContext, Future}

class UserAction @Inject()(parser: BodyParsers.Default, userService: UserService)(
  implicit executionContext: ExecutionContext
) {

  def forId(userId: UUID): ActionBuilder[Request, AnyContent] = new ActionBuilderImpl(parser) {
    override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
      recoverWith(ResponseCreator.exceptionMapper) {
        for {
          user <- userService.getUserById(userId)
          result <- block(UserRequest(user, request))
        } yield result
      }
  }
}
