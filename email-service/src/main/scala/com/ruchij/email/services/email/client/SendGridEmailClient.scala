package com.ruchij.email.services.email.client

import com.ruchij.email.services.email.EmailSerializer
import com.ruchij.email.services.email.models.Email
import com.ruchij.shared.ec.IOExecutionContext
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.{Method, Request, Response, SendGrid}
import scalaz.ReaderT

import scala.concurrent.{ExecutionContext, Future}

object SendGridEmailClient extends EmailClient[(SendGrid, IOExecutionContext), Mail, Response] {
  override def send[A](email: Email[A])(implicit emailSerializer: EmailSerializer[A, Mail], executionContext: ExecutionContext): ReaderT[Future, (SendGrid, IOExecutionContext), Response] =
    ReaderT {
      case (sendgrid, ioExecutionContext) =>
        Future {
          sendgrid.api {
            new Request {
              setMethod(Method.POST)
              setEndpoint("mail/send")
              setBody {
                emailSerializer.serialize(email).build()
              }
            }
          }
        }(ioExecutionContext)
    }
}
