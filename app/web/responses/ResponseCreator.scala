package web.responses

import exceptions.aggregation.{AggregatedExistingResourceException, AggregatedValidationException}
import exceptions.{ExistingEmailException, ExistingUsernameException, ResourceNotFoundException}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Result, Results}
import play.api.mvc.Results._
import web.responses.models.ExceptionResponse
import web.responses.models.ExceptionResponse.errorResponse

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

    case throwable => InternalServerError(errorResponse(throwable))
  }
}
