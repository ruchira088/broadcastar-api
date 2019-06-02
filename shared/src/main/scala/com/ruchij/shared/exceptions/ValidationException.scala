package com.ruchij.shared.exceptions

case class ValidationException(validationErrorMessage: String) extends Exception {
  override def getMessage: String = validationErrorMessage
}
