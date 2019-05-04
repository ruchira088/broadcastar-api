package exceptions

package object aggregation {
  def throwablesMessage(throwables: List[Exception]): String =
    throwables.map(_.getMessage).mkString("[ ", ", ", " ]")
}
