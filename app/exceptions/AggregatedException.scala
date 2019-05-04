package exceptions

case class AggregatedException[+A <: Throwable](throwables: List[A]) extends Exception {
  override def getMessage: String = throwables.map(_.getMessage).mkString("[ ", ", ", " ]")
}
