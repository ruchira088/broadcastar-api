package com.ruchij.shared.utils

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler, FileLock, OverlappingFileLockException}
import java.nio.file.{Path, StandardOpenOption}

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

object IOUtils {
  private val logger = Logger[IOUtils.type]

  def readFile(path: Path)(implicit executionContext: ExecutionContext): OptionT[Future, Array[Byte]] =
    if (path.toFile.exists()) {
      val promise = Promise[Array[Byte]]

      val fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)
      val byteBuffer = ByteBuffer.allocate(fileChannel.size().toInt)

      fileChannel.read(byteBuffer, 0, (): Unit, new CompletionHandler[Integer, Unit] {
        override def completed(result: Integer, attachment: Unit): Unit =
          promise.success(byteBuffer.array())

        override def failed(throwable: Throwable, attachment: Unit): Unit =
          promise.failure(throwable)
      })

      OptionT[Future, Array[Byte]] {
        promise.future
          .map(Some.apply)
          .andThen { case _ => fileChannel.close() }
      }
    }
    else
      OptionT.none[Future, Array[Byte]]

  def writeToFile(path: Path, data: Array[Byte], append: Boolean)(implicit executionContext: ExecutionContext): Future[Integer] = {
    val promise = Promise[Integer]

    path.toFile.createNewFile()
    val fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE)

    fileChannel.write(ByteBuffer.wrap(data), if (append) fileChannel.size() else 0, (): Unit, new CompletionHandler[Integer, Unit] {
      override def completed(result: Integer, attachment: Unit): Unit =
        promise.success(result)

      override def failed(throwable: Throwable, attachment: Unit): Unit =
        promise.failure(throwable)
    })

    promise.future.andThen { case _ => fileChannel.close() }
  }

  def lockFile[A](path: Path, action: => Future[A])(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Future[A] = {
    val promise = Promise[FileLock]

    val fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE)

    logger.debug(s"Acquiring file lock for ${path.toAbsolutePath}")

    acquireLock(path, fileChannel, RandomGenerator.finiteDurationGenerator(40), promise)

    for {
      fileLock <- promise.future
      result <- action
      _ <- Future.fromTry {
        Try {
          fileLock.release()
          logger.debug(s"Released file lock for ${path.toAbsolutePath}")

          fileChannel.close()
        }
      }
    }
    yield result
  }

  private def acquireLock(path: Path, fileChannel: AsynchronousFileChannel, delay: RandomGenerator[FiniteDuration], promise: Promise[FileLock])(implicit actorSystem: ActorSystem, executionContext: ExecutionContext): Unit =
    Try {
      logger.debug(s"Attempting file lock for ${path.toAbsolutePath}")

      fileChannel.lock((): Unit, new CompletionHandler[FileLock, Unit] {
        override def completed(fileLock: FileLock, attachment: Unit): Unit = {
          logger.debug(s"Successfully acquired file lock for ${path.toAbsolutePath}")
          promise.success(fileLock)
        }

        override def failed(throwable: Throwable, attachment: Unit): Unit = {
          logger.debug(s"Failed to acquired file lock for ${path.toAbsolutePath} (${throwable.getMessage})")
          promise.failure(throwable)
        }
      })
    }
      .recover {
        case _: OverlappingFileLockException =>
          val currentDelay = delay.generate()
          logger.debug(s"OverlappingFileLockException. Trying again in $currentDelay...")

          actorSystem.scheduler.scheduleOnce(currentDelay) {
            acquireLock(path, fileChannel, delay, promise)
          }
      }
}
