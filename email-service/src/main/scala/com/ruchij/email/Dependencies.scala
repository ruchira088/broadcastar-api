package com.ruchij.email

import com.ruchij.shared.ec.IOExecutionContext
import com.sendgrid.SendGrid

case class Dependencies(sendGrid: SendGrid, ioExecutionContext: IOExecutionContext)
