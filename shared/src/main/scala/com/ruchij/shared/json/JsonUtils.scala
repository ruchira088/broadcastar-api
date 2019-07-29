package com.ruchij.shared.json

import play.api.libs.json.{JsResult, JsResultException, Json, Writes}

import scala.util.{Failure, Success, Try}

object JsonUtils {
  def toTry[A](jsonResult: JsResult[A]): Try[A] =
    jsonResult.fold[Try[A]](
      validationErrors => Failure(JsResultException(validationErrors)),
      Success.apply
    )

  def prettyPrintJson[A : Writes](value: A): String =
    Json.prettyPrint {
      Json.toJson(value)
    }
}
