package dao.user

import java.util.UUID

import dao.user.models.DatabaseUser
import scalaz.OptionT

import scala.concurrent.{ExecutionContext, Future}

trait DatabaseUserDao {
  def insert(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[DatabaseUser]

  def getByUserId(userId: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser]

  def getByUsername(username: String)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser]

  def getByEmail(email: String)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser]

  def getByIndex(index: Long)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser]

  def verifyEmail(userId: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, Boolean]

  def update(userId: UUID, databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser]
}
