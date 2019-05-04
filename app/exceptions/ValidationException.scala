package exceptions

case class ValidationException(validationErrorMessage: String) extends Exception {
  override def getMessage: String = validationErrorMessage
}
