package dao

import play.api.db.slick.HasDatabaseConfigProvider
import slick.dbio.Effect
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.{ExecutionContext, Future}

trait InitializableTable {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  val TABLE_NAME: String

  protected def initializeCommand()(implicit executionContext: ExecutionContext): Future[Unit]

  def initialize()(implicit executionContext: ExecutionContext): Future[Boolean] =
    db.run(MTable.getTables(TABLE_NAME))
      .flatMap {
        tables =>
          if (tables.exists(_.name.name == TABLE_NAME))
            Future.successful(false)
          else
            initializeCommand().map(_ => true)
      }
}
