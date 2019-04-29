package exceptions

object FatalDatabaseException extends Exception {
  override def getMessage: String = "Fatal database exception"
}
