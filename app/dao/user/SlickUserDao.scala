package dao.user

import java.util.UUID

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
class SlickUserDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends UserDao
    with HasDatabaseConfigProvider[JdbcProfile] {
  import dao.SlickMappedColumns.dateTimeMappedColumn
  import dbConfig.profile.api._

  implicit val jdbcProfile: JdbcProfile = dbConfig.profile

  class UserTable(tag: Tag) extends Table[DatabaseUser](tag, SlickUserDao.TABLE_NAME) {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def firstName: Rep[String] = column[String]("first_name")
    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
    def email: Rep[String] = column[String]("email", O.Unique)
    def password: Rep[String] = column[String]("password")
    def emailVerified: Rep[Boolean] = column[Boolean]("email_verified")

    override def * : ProvenShape[DatabaseUser] =
      (id, createdAt, firstName, lastName, email, password, emailVerified) <> (DatabaseUser.apply _ tupled, DatabaseUser.unapply)
  }

  val users = TableQuery[UserTable]

  override def insert(databaseUser: DatabaseUser)(implicit executionContext: ExecutionContext): Future[DatabaseUser] =
    db.run(users += databaseUser)
      .flatMap {
        _ => getById(databaseUser.id) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getById(id: UUID)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    OptionT {
      db.run(users.filter(_.id === id).take(1).result)
        .map(_.headOption)
    }


  override def getByEmail(email: String)(implicit executionContext: ExecutionContext): OptionT[Future, DatabaseUser] =
    OptionT {
      db.run(users.filter(_.email === email).take(1).result)
        .map(_.headOption)
    }

  override def verifiedEmail(email: String)(implicit executionContext: ExecutionContext): OptionT[Future, Boolean] =
    getByEmail(email)
      .flatMap {
        databaseUser =>
          if (databaseUser.emailVerified)
            OptionT.some[Future, Boolean](true)
          else
            OptionT[Future, Boolean] {
              db.run(users.filter(_.email === email).map(_.emailVerified).update(true))
                .map(_ => Some(false))
            }
      }
}

object SlickUserDao {
  val TABLE_NAME = "users"
}
