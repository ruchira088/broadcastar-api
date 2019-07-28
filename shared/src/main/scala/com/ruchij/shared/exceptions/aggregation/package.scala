package com.ruchij.shared.exceptions

package object aggregation {
  def throwablesMessage(throwables: List[Throwable]): String =
    throwables.map(_.getMessage).mkString("[ ", ", ", " ]")
}
