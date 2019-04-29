package utils
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object TestUtils {
  def waitForResult[A](future: Future[A]): A =
    Await.result(future, Duration(10, TimeUnit.SECONDS))
}