package dao.offset

import scalaz.OptionT
import services.triggering.models.{Offset, OffsetType}

import scala.concurrent.{ExecutionContext, Future}

trait OffsetDao {
  def insert(offset: Offset)(implicit executionContext: ExecutionContext): Future[Offset]

  def acquireOffsetLock(offsetType: OffsetType)(implicit executionContext: ExecutionContext): OptionT[Future, Offset]

  def releaseOffsetLock(offsetType: OffsetType, offsetValue: Long)(implicit executionContext: ExecutionContext): OptionT[Future, Offset]

  def getLatestOffset(offsetType: OffsetType)(implicit executionContext: ExecutionContext): OptionT[Future, Offset]
}
