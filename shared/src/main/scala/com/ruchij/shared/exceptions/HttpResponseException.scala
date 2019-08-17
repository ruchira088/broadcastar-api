package com.ruchij.shared.exceptions

import play.api.libs.ws.WSRequest

case class HttpResponseException(status: Int, statusText: String, body: String) extends Exception {
  override def getMessage: String = s"$status $statusText $body"
}

object HttpResponseException {
  def apply(response: WSRequest#Response): HttpResponseException =
    HttpResponseException(response.status, response.statusText, response.body)
}
