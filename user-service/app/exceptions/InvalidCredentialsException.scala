package exceptions

object InvalidCredentialsException extends Exception {
  override def getMessage: String = "Invalid credentials"
}
