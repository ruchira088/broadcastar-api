package dao.user

import java.util.UUID

import dao.InitializableTable
import dao.user.models.DatabaseUser
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import utils.MonadicUtils.OptionTWrapper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickDatabaseUserDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends DatabaseUserDao
    with HasDatabaseConfigProvider[JdbcProfile] with InitializableTable {

  import dbConfig.profile.api._
  import dao.SlickMappedColumns.dateTimeMappedColumn

  override val TABLE_NAME: String = "users"

  class UserTable(tag: Tag) extends Table[DatabaseUser](tag, TABLE_NAME) {
    def userId: Rep[UUID] = column[UUID]("user_id", O.PrimaryKey)
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def username: Rep[String] = column[String]("username", O.Unique)
    def firstName: Rep[String] = column[String]("first_name")
    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
    def email: Rep[String] = column[String]("email", O.Unique)
    def password: Rep[String] = column[String]("password")
    def profileImageId: Rep[Option[String]] = column[Option[String]]("profile_image_id")
    def emailVerified: Rep[Boolean] = column[Boolean]("email_verified")

    override def * : ProvenShape[DatabaseUser] =
      (userId, createdAt, username, firstName, lastName, email, password, profileImageId, emailVerified) <> (DatabaseUser.apply _ tupled, DatabaseUser.unapply)
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

  override protected def initializeCommand()(implicit executionContext: ExecutionContext): Future[Unit] = db.run(users.schema.createIfNotExists)

}
