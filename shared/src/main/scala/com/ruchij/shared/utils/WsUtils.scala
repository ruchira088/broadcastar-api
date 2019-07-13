package com.ruchij.shared.utils

import com.ruchij.shared.exceptions.HttpResponseException
import com.ruchij.shared.json.JsonUtils
import play.api.http.Status
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.Future
import scala.util.{Failure, Try}

object WsUtils {

  object SuccessResponse {
    def unapply(response: WSResponse): Option[WSResponse] =
      if (Status.isSuccessful(response.status)) Some(response) else None
  }

  private def responseTo[A: Reads](response: WSResponse): Try[A] =
    Try(response.json)
      .flatMap {
        json => JsonUtils.toTry {
          Json.fromJson[A](json)
        }
      }

  private def handle[A: Reads]: PartialFunction[WSResponse, Try[A]] = {
    case SuccessResponse(response) => responseTo[A](response)

    case response => Failure(HttpResponseException(response))
  }

  def handleResponse[A: Reads](response: WSResponse): Future[A] = handle[A].andThen(Future.fromTry[A])(response)
}
