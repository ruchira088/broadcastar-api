package services.triggering.models

import java.util.UUID

import org.joda.time.DateTime

case class Offset(id: UUID, createdAt: DateTime, offsetType: OffsetType, value: Long, lockAcquiredAt: Option[DateTime])
