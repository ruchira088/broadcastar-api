package utils

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, CompletionHandler}
import java.nio.file.{Path, StandardOpenOption}

import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance

import scala.concurrent.{ExecutionContext, Future, Promise}

object IOUtils {
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

  def writeToFile(path: Path, data: Array[Byte])(implicit executionContext: ExecutionContext): Future[Integer] = {
    val promise = Promise[Integer]

    path.toFile.createNewFile()
    val fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE)

    fileChannel.write(ByteBuffer.wrap(data), 0, (): Unit, new CompletionHandler[Integer, Unit] {
      override def completed(result: Integer, attachment: Unit): Unit =
        promise.success(result)

      override def failed(throwable: Throwable, attachment: Unit): Unit =
        promise.failure(throwable)
    })

    promise.future.andThen { case _ => fileChannel.close() }
  }
}
