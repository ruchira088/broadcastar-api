package com.ruchij.shared.exceptions

case class FatalException(message: String) extends Exception {
  override def getMessage: String = message
}
