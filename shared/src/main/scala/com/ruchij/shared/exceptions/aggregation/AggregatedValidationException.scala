package com.ruchij.shared.exceptions.aggregation

import com.ruchij.shared.exceptions.ValidationException

case class AggregatedValidationException(throwables: List[ValidationException]) extends Exception {
  override def getMessage: String = throwablesMessage(throwables)
}
