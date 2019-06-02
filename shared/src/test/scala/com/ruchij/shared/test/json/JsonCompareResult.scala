package com.ruchij.shared.test.json

import com.ruchij.shared.test.json.JsonDifference.{AdditionalJsonKey, JsonValueMismatch, MissingJsonKey}

case class JsonCompareResult(
  mismatchedValues: List[JsonValueMismatch],
  missingKeys: List[MissingJsonKey],
  additionalKeys: List[AdditionalJsonKey]
) {
  def +(jsonCompareResult: JsonCompareResult): JsonCompareResult =
    JsonCompareResult(
      mismatchedValues ++ jsonCompareResult.mismatchedValues,
      missingKeys ++ jsonCompareResult.missingKeys,
      additionalKeys ++ jsonCompareResult.additionalKeys
    )

  override def toString: String =
    (mismatchedValues ++ missingKeys ++ additionalKeys).mkString("[\n  ", ",\n  ", "\n]")
}

object JsonCompareResult {
  val empty = JsonCompareResult(List.empty, List.empty, List.empty)
}
