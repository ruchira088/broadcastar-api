package com.ruchij.shared.utils

import java.util.UUID

import org.joda.time.{DateTime, DateTimeZone}

trait SystemUtilities {
  def currentTime(): DateTime = DateTime.now().withZone(DateTimeZone.UTC)

  def randomUuid(): UUID = UUID.randomUUID()
}

object SystemUtilities extends SystemUtilities
