package com.ruchij.shared.test

import com.ruchij.shared.test.json.JsonDifference
import com.ruchij.shared.test.utils.Matchers
import play.api.libs.json.Json

object TestMain {
  def main(args: Array[String]): Unit = {
    val jsonA =
      """{
        | "name": "John",
        | "age": 1,
        | "address": {
        |   "unit": 1,
        |   "state": "NSW",
        |   "codes": ["a", "b", "c"]
        | }
        |}""".stripMargin

    val jsonB =
      """{
        | "name": "Ann",
        | "email": "sample@email.com",
        | "address": {
        |   "unit": 2,
        |   "state": false,
        |   "codes": ["a", "c", "d", "e"]
        | }
        |}""".stripMargin

    println {
      JsonDifference.compare(Json.parse(jsonA), Json.parse(jsonB))
    }
  }
}
