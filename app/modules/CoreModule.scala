package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import config.SystemUtilities
import dao.user.{DatabaseUserDao, SlickDatabaseUserDao}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._
import scala.language.postfixOps

class CoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
  }

  @Singleton
  @Provides
  def databaseUserDao(slickDatabaseUserDao: SlickDatabaseUserDao)(implicit executionContext: ExecutionContext): DatabaseUserDao =
    Await.result(slickDatabaseUserDao.initialize().map(createdTable => slickDatabaseUserDao), 30 seconds)
}
