package com.ruchij.email.services.email.models

import com.ruchij.email.services.email.EmailType

case class Email[+A](from: String, to: String, subject: String, content: A)

object Email {
  def create[A, B](value: A)(implicit emailType: EmailType[A, B]): Email[B] = emailType.email(value)
}
