package exceptions.aggregation

import exceptions.ExistingResourceException

case class AggregatedExistingResourceException(throwables: List[ExistingResourceException]) extends ExistingResourceException {
  override def getMessage: String = throwablesMessage(throwables)
}
