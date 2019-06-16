package services.triggering.models

import com.ruchij.enum.Enum

sealed trait OffsetType extends Enum

object OffsetType {
  case object UserCreated extends OffsetType
}