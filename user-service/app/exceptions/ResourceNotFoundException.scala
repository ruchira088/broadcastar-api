package exceptions

case class ResourceNotFoundException(errorMessage: String) extends Exception {
  override def getMessage: String = errorMessage
}
