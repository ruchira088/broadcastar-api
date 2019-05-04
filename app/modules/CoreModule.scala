package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import config.SystemUtilities
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}
import ec.{BlockingExecutionContext, BlockingExecutionContextImpl}
import services.crypto.{BCryptService, CryptographyService}
import services.user.{UserService, UserServiceImpl}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

class CoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[CryptographyService]).to(classOf[BCryptService])
    bind(classOf[BlockingExecutionContext]).to(classOf[BlockingExecutionContextImpl])
  }

  @Singleton
  @Provides
  def databaseUserDao(slickDatabaseUserDao: SlickDatabaseUserDao)(implicit executionContext: ExecutionContext): DatabaseUserDao =
    Await.result(slickDatabaseUserDao.initialize().map(createdTable => slickDatabaseUserDao), 30 seconds)
}
