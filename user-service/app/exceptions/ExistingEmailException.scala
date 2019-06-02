package exceptions

import com.ruchij.shared.exceptions.ExistingResourceException

case class ExistingEmailException(email: String) extends ExistingResourceException {
  override def getMessage: String = s"Email address is already registered to an existing user: $email"
}
