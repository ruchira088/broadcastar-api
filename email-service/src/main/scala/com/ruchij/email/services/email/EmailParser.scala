package com.ruchij.email.services.email

import com.ruchij.email.services.email.models.Email
import com.ruchij.shared.kafka.models.VerificationEmail
import play.twirl.api.HtmlFormat

import scala.util.{Success, Try}

sealed trait EmailParser[-A, +B] {
  def email(value: A): Try[Email[B]]
}

object EmailParser {
  implicit case object VerifyEmail extends EmailParser[VerificationEmail, HtmlFormat.Appendable] {
    override def email(verificationEmail: VerificationEmail): Try[Email[HtmlFormat.Appendable]] =
      Success {
        Email(
          "Chirper <welcome@chirper.ruchij.com>",
          "ruchira088@gmail.com",
          //        verificationEmail.user.email,
          "Welcome to Chirper",
          html.verifyEmail(verificationEmail.user.firstName, verificationEmail.emailVerificationToken.secret)
        )
      }
  }
}
