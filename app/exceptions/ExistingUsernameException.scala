package exceptions

case class ExistingUsernameException(username: String) extends ExistingResourceException {
  override def getMessage: String = s"Username already exists: $username"
}
