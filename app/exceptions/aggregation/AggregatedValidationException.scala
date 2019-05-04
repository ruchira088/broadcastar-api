package exceptions.aggregation

import exceptions.ValidationException

case class AggregatedValidationException(throwables: List[ValidationException]) extends Exception {
  override def getMessage: String = throwablesMessage(throwables)
}
