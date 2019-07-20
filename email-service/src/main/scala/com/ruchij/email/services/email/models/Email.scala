package com.ruchij.email.services.email.models

case class Email[+A](from: String, to: String, subject: String, content: A)
