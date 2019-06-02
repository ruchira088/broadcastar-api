package com.ruchij.shared.test.json

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

sealed trait JsonDifference {
  val key: List[String]
}

object JsonDifference {
  case class JsonValueMismatch(key: List[String], valueA: JsValue, valueB: JsValue) extends JsonDifference {
    override def toString: String = s"At ${key.mkString(".")}: ${Json.prettyPrint(valueA)} Vs ${Json.prettyPrint(valueB)}"
  }

  case class MissingJsonKey(key: List[String]) extends JsonDifference {
    override def toString: String = s"Missing key: ${key.mkString(".")}"
  }

  case class AdditionalJsonKey(key: List[String]) extends JsonDifference {
    override def toString: String = s"Additional key: ${key.mkString(".")}"
  }

  def lift: PartialFunction[JsonDifference, JsonCompareResult] = {
    case valueMismatch: JsonValueMismatch => JsonCompareResult(List(valueMismatch), List.empty, List.empty)
    case missingJsonKey: MissingJsonKey => JsonCompareResult(List.empty, List(missingJsonKey), List.empty)
    case additionalJsonKey: AdditionalJsonKey => JsonCompareResult(List.empty, List.empty, List(additionalJsonKey))
  }

  def compare(primary: JsValue, secondary: JsValue, path: List[String] = List.empty): JsonCompareResult =
    (primary, secondary) match {
      case (a, b) if a == b => JsonCompareResult.empty

      case (JsObject(a), JsObject(b)) =>
        val additionJsonKeys =
          a.keySet.diff(b.keySet).foldLeft(JsonCompareResult.empty) {
            (result, key) => result + lift(AdditionalJsonKey(path :+ key))
          }

        val missingJsonKeys =
          b.keySet.diff(a.keySet).foldLeft(JsonCompareResult.empty) {
            (result, key) => result + lift(MissingJsonKey(path :+ key))
          }

        val diff =
          a.keySet.intersect(b.keySet)
            .map {
              key => (key, a.get(key), b.get(key))
            }
            .collect {
              case (key, Some(jsA), Some(jsB)) => compare(jsA, jsB, path :+ key)
            }
            .fold(JsonCompareResult.empty) { _ + _ }

        diff + additionJsonKeys + missingJsonKeys

      case (JsArray(a), JsArray(b)) =>
        val diff =
          a.zip(b).zipWithIndex.toList
            .map {
              case ((jsA, jsB), index) => compare(jsA, jsB, path :+ index.toString)
            }
            .foldLeft(JsonCompareResult.empty) { _ + _ }

        val missingOrAdditional =
          if (a.length > b.length)
            a.zipWithIndex.drop(b.length).toList
              .map {
                case (_, index) => lift(AdditionalJsonKey(path :+ index.toString))
              }
          else
            b.zipWithIndex.drop(a.length).toList
              .map {
                case (_, index) => lift(MissingJsonKey(path :+ index.toString))
              }

        diff + missingOrAdditional.foldLeft(JsonCompareResult.empty) { _ + _ }

      case (a, b) =>
        JsonCompareResult(List(JsonValueMismatch(path, a, b)), List.empty, List.empty)
    }
}
