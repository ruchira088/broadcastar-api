package com.ruchij.email.services.email

import com.ruchij.email.services.email.models.Email
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.{Content, Email => EmailAddress}
import play.twirl.api.{HtmlFormat, MimeTypes}

trait EmailSerializer[-A, +B] {
  def serialize(email: Email[A]): B
}

object EmailSerializer {
  implicit val sendGridEmailSerializer: EmailSerializer[HtmlFormat.Appendable, Mail] =
    (email: Email[HtmlFormat.Appendable]) =>
      new Mail(
        new EmailAddress(email.from),
        email.subject,
        new EmailAddress(email.to),
        new Content(MimeTypes.HTML, email.content.body)
    )

  implicit val stubEmailSerializer: EmailSerializer[HtmlFormat.Appendable, String] =
    (email: Email[HtmlFormat.Appendable]) =>
      s"""
        |---------------------------
        |From: ${email.from}
        |To: ${email.to}
        |Subject: ${email.subject}
        |
        |Body:
        |${email.content.body}
        |---------------------------
        |""".stripMargin
}
