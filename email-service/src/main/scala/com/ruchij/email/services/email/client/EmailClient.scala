package com.ruchij.email.services.email.client

import com.ruchij.email.Dependencies
import com.ruchij.email.services.email.EmailSerializer
import com.ruchij.email.services.email.models.Email
import scalaz.ReaderT

import scala.concurrent.{ExecutionContext, Future}

trait EmailClient[ClientMessage, Output] {
  type Input

  def local(dependencies: Dependencies): Input

  def send[A](email: Email[A])(
    implicit emailSerializer: EmailSerializer[A, ClientMessage],
    executionContext: ExecutionContext
  ): ReaderT[Future, Input, Output]
}
