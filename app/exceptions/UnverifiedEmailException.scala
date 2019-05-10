package exceptions

case class UnverifiedEmailException(email: String) extends Exception {
  override def getMessage: String = s"Email address has not been verified: $email"
}
