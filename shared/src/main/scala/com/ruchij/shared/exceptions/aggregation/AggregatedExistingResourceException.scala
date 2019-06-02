package com.ruchij.shared.exceptions.aggregation

import com.ruchij.shared.exceptions.ExistingResourceException

case class AggregatedExistingResourceException(throwables: List[ExistingResourceException]) extends ExistingResourceException {
  override def getMessage: String = throwablesMessage(throwables)
}
