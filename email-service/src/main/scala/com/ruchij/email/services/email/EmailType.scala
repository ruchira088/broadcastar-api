package com.ruchij.email.services.email

import com.ruchij.email.services.email.models.Email
import com.ruchij.shared.kafka.models.VerificationEmail
import play.twirl.api.HtmlFormat

sealed trait EmailType[-A, +B] {
  def email(value: A): Email[B]
}

object EmailType {
  implicit case object VerifyEmail extends EmailType[VerificationEmail, HtmlFormat.Appendable] {
    override def email(verificationEmail: VerificationEmail): Email[HtmlFormat.Appendable] =
      Email(
        "Chirper <welcome@chirper.ruchij.com>",
        "ruchira088@gmail.com",
//        verificationEmail.user.email,
        "Welcome to Chirper",
        html.verifyEmail(verificationEmail.user.firstName, verificationEmail.emailVerificationToken.secret)
      )
  }
}
