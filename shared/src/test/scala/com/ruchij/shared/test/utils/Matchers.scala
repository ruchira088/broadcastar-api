package com.ruchij.shared.test.utils

import com.ruchij.shared.test.json.{JsonCompareResult, JsonDifference}
import com.ruchij.shared.test.json.JsonDifference.{AdditionalJsonKey, JsonValueMismatch, MissingJsonKey, lift}
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.http.ContentTypes
import play.api.libs.json._

import scala.util.Try

object Matchers {
  def beJson: Matcher[Option[String]] =
    (left: Option[String]) =>
      MatchResult(
        left.contains(ContentTypes.JSON),
        left.fold("ContentType was empty")(contentType => s"$contentType != ${ContentTypes.JSON}"),
        s"ContentType was ${ContentTypes.JSON}"
      )

  def equalJsonOf[A](value: A)(implicit writes: OWrites[A]): Matcher[JsValue] =
    (left: JsValue) =>
      MatchResult(
        writes.writes(value) == left,
        JsonDifference.compare(writes.writes(value), left).toString,
        "Json values are equal"
      )

  def equalJson(json: String): Matcher[JsValue] =
    (left: JsValue) =>
      MatchResult(
        Try(Json.parse(json)).map(_ == left).getOrElse(false),
        Try(Json.parse(json)).fold[String](_.getMessage, JsonDifference.compare(_, left).toString),
        "Json values are equal"
      )
}
