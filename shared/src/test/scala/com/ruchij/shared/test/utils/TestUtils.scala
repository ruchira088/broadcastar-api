package com.ruchij.shared.test.utils

import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.mvc.Headers
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}

object TestUtils {
  val DEFAULT_HEADERS = FakeHeaders(List(HOST -> "localhost"))

  def postRequest[A: OWrites](url: String, body: A, headers: Headers = FakeHeaders()): FakeRequest[JsObject] =
    FakeRequest[JsObject](POST, url, DEFAULT_HEADERS.add(headers.headers: _*), Json.toJsObject(body))
}
