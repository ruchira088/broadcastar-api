package web.responses

import com.ruchij.shared.exceptions.aggregation.{AggregatedExistingResourceException, AggregatedValidationException}
import com.ruchij.shared.web.responses.models.ExceptionResponse
import com.ruchij.shared.web.responses.models.ExceptionResponse.errorResponse
import exceptions.{ExistingEmailException, ExistingUsernameException, InvalidCredentialsException, ResourceNotFoundException}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.Results._
import play.api.mvc.{Result, Results}

import scala.concurrent.{ExecutionContext, Future}

object ResponseCreator {
  def create[A: Writes](status: Results#Status)(valueFuture: Future[A])(implicit executionContext: ExecutionContext): Future[Result] =
    valueFuture
      .map {
        value => status(Json.toJson(value))
      }
      .recover(exceptionMapper)

  val exceptionMapper: PartialFunction[Throwable, Result] = {
    case existingUsernameException: ExistingUsernameException => Conflict(errorResponse(existingUsernameException))

    case existingEmailException: ExistingEmailException => Conflict(errorResponse(existingEmailException))

    case resourceNotFoundException: ResourceNotFoundException => NotFound(errorResponse(resourceNotFoundException))

    case AggregatedValidationException(validationExceptions) =>
      BadRequest { Json.toJsObject(ExceptionResponse(validationExceptions)) }

    case AggregatedExistingResourceException(existingResourceExceptions) =>
      Conflict { Json.toJsObject(ExceptionResponse(existingResourceExceptions)) }

    case InvalidCredentialsException => Unauthorized(errorResponse(InvalidCredentialsException))

    case throwable => InternalServerError(errorResponse(throwable))
  }
}
