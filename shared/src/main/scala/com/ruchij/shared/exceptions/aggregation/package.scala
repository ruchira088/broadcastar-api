package com.ruchij.shared.exceptions

package object aggregation {
  def throwablesMessage(throwables: List[Exception]): String =
    throwables.map(_.getMessage).mkString("[ ", ", ", " ]")
}
