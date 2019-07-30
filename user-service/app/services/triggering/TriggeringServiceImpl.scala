package services.triggering

import akka.actor.Cancellable
import akka.stream.scaladsl.Source
import com.google.inject.{Inject, Singleton}
import com.ruchij.shared.utils.SystemUtilities
import com.typesafe.scalalogging.Logger
import config.TriggerConfiguration
import dao.offset.OffsetDao
import dao.user.DatabaseUserDao
import dao.user.models.DatabaseUser
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import services.triggering.models.{Offset, OffsetType}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class TriggeringServiceImpl @Inject()(
  databaseUserDao: DatabaseUserDao,
  offsetDao: OffsetDao,
  triggerConfiguration: TriggerConfiguration
)(implicit systemUtilities: SystemUtilities)
    extends TriggeringService {

  private val logger = Logger[TriggeringServiceImpl]

  override def userCreated()(implicit executionContext: ExecutionContext): Source[DatabaseUser, Cancellable] =
    Source
      .tick(triggerConfiguration.initialDelay, triggerConfiguration.pollingInterval, (): Unit)
      .mapAsync(1) { _ =>
        offsetDao
          .acquireOffsetLock(OffsetType.UserCreated)
          .orElse {
            offsetDao
              .getLatestOffset(OffsetType.UserCreated)
              .flatMap { offset =>
                offset.lockAcquiredAt.fold(OptionT.none[Future, Offset]) { lockAcquiredAt =>
                  if (systemUtilities
                      .currentTime()
                      .isAfter(lockAcquiredAt.plus(triggerConfiguration.offsetLockTimeout.toMillis)))
                    offsetDao
                      .releaseOffsetLock(offset.offsetType, offset.value)
                      .flatMap {
                        offset =>
                          logger.warn(s"Released offset lock due to time-out Offset(value = ${offset.value}, lockAcquiredAt=${offset.lockAcquiredAt})")
                          OptionT.none[Future, Offset]
                      }
                  else
                    OptionT.none[Future, Offset]
                }
              }
          }
          .flatMap { offset =>
            databaseUserDao.getByIndex(offset.value)
              .orElse {
                offsetDao.releaseOffsetLock(offset.offsetType, offset.value)
                  .flatMap { _ => OptionT.none }
              }
          }
          .run
      }
      .mapConcat[DatabaseUser] {
        case Some(databaseUser) =>
          logger.info(s"New user created: ${databaseUser.username}")
          List(databaseUser)

        case None => List.empty
      }

  override def commitUserCreated(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[Offset] =
    commit(OffsetType.UserCreated, databaseUser.index)

  private def commit(offsetType: OffsetType, offsetValue: Long)(implicit executionContext: ExecutionContext): Future[Offset] =
    offsetDao.insert {
      Offset(systemUtilities.randomUuid(), systemUtilities.currentTime(), offsetType, offsetValue + 1, None)
    }
}
