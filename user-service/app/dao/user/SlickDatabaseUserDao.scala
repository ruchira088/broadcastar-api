package dao.user

import java.util.UUID

import com.ruchij.shared.monads.MonadicUtils.OptionTWrapper
import dao.user.models.DatabaseUser
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickDatabaseUserDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends DatabaseUserDao
    with HasDatabaseConfigProvider[JdbcProfile] {

  import dao.SlickMappedColumns.dateTimeMappedColumn
  import dbConfig.profile.api._

  class UserTable(tag: Tag) extends Table[DatabaseUser](tag, SlickDatabaseUserDao.TABLE_NAME) {
    def userId: Rep[UUID] = column[UUID]("user_id", O.Unique)
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def index: Rep[Long] = column[Long]("index", O.AutoInc)
    def username: Rep[String] = column[String]("username", O.Unique)
    def firstName: Rep[String] = column[String]("first_name")
    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
    def email: Rep[String] = column[String]("email", O.Unique)
    def password: Rep[String] = column[String]("password")
    def profileImageId: Rep[Option[String]] = column[Option[String]]("profile_image_id")
    def emailVerified: Rep[Boolean] = column[Boolean]("email_verified")

    override def * : ProvenShape[DatabaseUser] =
      (userId, createdAt, index, username, firstName, lastName, email, password, profileImageId, emailVerified) <> (DatabaseUser.apply _ tupled, DatabaseUser.unapply)
  }

  val users = TableQuery[UserTable]

  override def insert(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[DatabaseUser] =
    db.run(users += databaseUser)
      .flatMap {
        _ => getByUserId(databaseUser.userId) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getByUserId(id: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    getBySelector(_.userId === id)

  override def getByUsername(username: String)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    getBySelector(_.username === username)

  override def getByEmail(email: String)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    getBySelector(_.email === email)

  override def getByIndex(index: Long)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    getBySelector(_.index === index)

  private def getBySelector(selector: UserTable => Rep[Boolean])(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    OptionT {
      db.run(users.filter(selector).take(1).result).map(_.headOption)
    }

  override def verifyEmail(id: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, Boolean] =
    getByUserId(id)
      .flatMap {
        databaseUser =>
          if (databaseUser.emailVerified)
            OptionT.some[Future, Boolean](true)
          else
            OptionT[Future, Boolean] {
              db.run(users.filter(_.userId === id).map(_.emailVerified).update(true))
                .map(_ => Some(false))
            }
      }

  override def update(userId: UUID, databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    getByUserId(userId)
      .flatMapF {
        _ => db.run { users.filter(_.userId === userId).update(databaseUser) }
      }
      .flatMap(_ => getByUserId(databaseUser.userId))
}

object SlickDatabaseUserDao {
  val TABLE_NAME = "users"
}
