package services.crypto
import ec.BlockingExecutionContext
import javax.inject.{Inject, Singleton}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BCryptService @Inject()(blockingExecutionContext: BlockingExecutionContext) extends CryptographyService {
  override def hashPassword(password: String)(implicit executionContext: ExecutionContext): Future[String] =
    Future {
      BCrypt.hashpw(password, BCrypt.gensalt())
    }(blockingExecutionContext)

  override def checkPassword(candidate: String, passwordHash: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    Future {
      BCrypt.checkpw(candidate, passwordHash)
    }(blockingExecutionContext)
}
