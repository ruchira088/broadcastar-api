package exceptions

case class AggregatedValidationException(throwables: List[ValidationException]) extends Exception {
  override def getMessage: String = throwables.map(_.getMessage).mkString("[ ", ", ", " ]")
}
