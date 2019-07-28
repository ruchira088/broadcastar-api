package com.ruchij.shared.exceptions.aggregation

case class AggregatedConfigurationException(throwables: List[Throwable]) extends Exception {
  override def getMessage: String = throwablesMessage(throwables)
}
