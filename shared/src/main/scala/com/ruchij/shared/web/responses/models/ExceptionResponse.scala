package com.ruchij.shared.web.responses.models

import play.api.libs.json._

case class ExceptionResponse(errors: List[Throwable])

object ExceptionResponse {
  implicit val throwableWrites: Writes[Throwable] = (throwable: Throwable) => JsString(throwable.getMessage)

  implicit val exceptionResponseWrites: OWrites[ExceptionResponse] = Json.writes[ExceptionResponse]

  def errorResponse(throwable: Throwable): JsObject = Json.toJsObject(ExceptionResponse(List(throwable)))
}
