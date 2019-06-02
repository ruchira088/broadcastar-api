package exceptions

object FatalWebServerException extends Exception {
  override def getMessage: String = "Fatal web server exception"
}
