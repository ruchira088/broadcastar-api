package com.ruchij.shared.web.requests

import play.api.libs.json.{JsResultException, JsValue, Reads}
import play.api.mvc.Request

import scala.util.{Failure, Try}

object RequestParser {
  def parse[A](request: Request[JsValue])(implicit jsonReads: Reads[A], validator: Validator[A]): Try[A] =
    jsonReads.reads(request.body)
      .fold(validationErrors => Failure(JsResultException(validationErrors)), validator.validate)
}
