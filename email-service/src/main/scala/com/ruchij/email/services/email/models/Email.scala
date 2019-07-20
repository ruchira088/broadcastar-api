package com.ruchij.email.services.email.models

import com.ruchij.email.services.email.EmailParser

import scala.util.Try

case class Email[+A](from: String, to: String, subject: String, content: A)

object Email {
  def create[A, B](value: A)(implicit emailType: EmailParser[A, B]): Try[Email[B]] = emailType.email(value)
}
