package com.ruchij.email.services.email.client
import com.ruchij.email.Dependencies
import com.ruchij.email.services.email.EmailSerializer
import com.ruchij.email.services.email.models.Email
import scalaz.ReaderT

import scala.concurrent.{ExecutionContext, Future}

object StubEmailClient extends EmailClient[String, Unit] {
  override type Input = Unit

  override def local(dependencies: Dependencies): Input = (): Unit

  override def send[A](email: Email[A])(implicit emailSerializer: EmailSerializer[A, String], executionContext: ExecutionContext): ReaderT[Future, Input, Unit] =
    ReaderT {
      _ =>
        Future.successful {
          println {
            emailSerializer.serialize(email)
          }
        }
    }
}
