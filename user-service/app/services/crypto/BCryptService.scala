package services.crypto
import ec.CpuIntensiveExecutionContext
import javax.inject.{Inject, Singleton}
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BCryptService @Inject()(cpuIntensiveExecutionContext: CpuIntensiveExecutionContext) extends CryptographyService {
  override def hashPassword(password: String)(implicit executionContext: ExecutionContext): Future[String] =
    Future {
      BCrypt.hashpw(password, BCrypt.gensalt())
    }(cpuIntensiveExecutionContext)

  override def checkPassword(candidate: String, passwordHash: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    Future {
      BCrypt.checkpw(candidate, passwordHash)
    }(cpuIntensiveExecutionContext)
}
