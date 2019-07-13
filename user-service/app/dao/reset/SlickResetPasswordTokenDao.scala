package dao.reset

import java.util.UUID

import com.ruchij.shared.monads.MonadicUtils.OptionTWrapper
import dao.reset.models.ResetPasswordToken
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import slick.jdbc.JdbcProfile
import slick.lifted.{PrimaryKey, ProvenShape}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickResetPasswordTokenDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with ResetPasswordTokenDao {

  import dao.SlickMappedColumns.dateTimeMappedColumn
  import dbConfig.profile.api._

  class ResetPasswordTokenTable(tag: Tag) extends Table[ResetPasswordToken](tag, SlickResetPasswordTokenDao.TABLE_NAME) {
    def userId: Rep[UUID] = column[UUID]("user_id")
    def secret: Rep[UUID] = column[UUID]("secret")
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def email: Rep[String] = column[String]("email")
    def expiresAt: Rep[DateTime] = column[DateTime]("expires_at")
    def resetAt: Rep[Option[DateTime]] = column[Option[DateTime]]("reset_at")

    def pk: PrimaryKey = primaryKey("reset_password_tokens_pkey", (userId, secret))

    override def * : ProvenShape[ResetPasswordToken] =
      (userId, secret, createdAt, email, expiresAt, resetAt) <> (ResetPasswordToken.apply _ tupled, ResetPasswordToken.unapply)
  }

  val resetPasswordTokens = TableQuery[ResetPasswordTokenTable]

  override def insert(
    resetPasswordToken: ResetPasswordToken
  )(implicit executionContext: ExecutionContext): Future[ResetPasswordToken] =
    db.run(resetPasswordTokens += resetPasswordToken)
      .flatMap { _ =>
        getByUserIdAndSecret(resetPasswordToken.userId, resetPasswordToken.secret) ifEmpty Future.failed(
          FatalDatabaseException
        )
      }

  override def getByUserIdAndSecret(userId: UUID, secret: UUID)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, ResetPasswordToken] =
    OptionT {
      db.run {
          resetPasswordTokens
            .filter(resetPasswordToken => resetPasswordToken.userId === userId && resetPasswordToken.secret === secret)
            .take(1)
            .result
        }
        .map(_.headOption)
    }
}

object SlickResetPasswordTokenDao {
  val TABLE_NAME = "reset_password_tokens"
}
