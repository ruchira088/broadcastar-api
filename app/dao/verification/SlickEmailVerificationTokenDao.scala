package dao.verification

import java.util.UUID

import dao.InitializableTable
import dao.verification.models.EmailVerificationToken
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import slick.jdbc.JdbcProfile
import slick.lifted.{PrimaryKey, ProvenShape}
import utils.SystemUtilities
import utils.MonadicUtils.OptionTWrapper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickEmailVerificationTokenDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit systemUtilities: SystemUtilities
) extends HasDatabaseConfigProvider[JdbcProfile]
    with EmailVerificationTokenDao
    with InitializableTable {

  import dbConfig.profile.api._
  import dao.SlickMappedColumns.dateTimeMappedColumn

  override val TABLE_NAME: String = "email_verification_tokens"

  class EmailVerificationTokenTable(tag: Tag) extends Table[EmailVerificationToken](tag, TABLE_NAME) {
    def userId: Rep[UUID] = column[UUID]("user_id")
    def secret: Rep[UUID] = column[UUID]("secret")
    def email: Rep[String] = column[String]("email")
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def verifiedAt: Rep[Option[DateTime]] = column[Option[DateTime]]("verified_at")

    def pk: PrimaryKey = primaryKey("email_verification_tokens_pkey", (userId, secret))

    override def * : ProvenShape[EmailVerificationToken] =
      (userId, secret, email, createdAt, verifiedAt) <> (EmailVerificationToken.apply _ tupled, EmailVerificationToken.unapply)
  }

  val emailVerificationTokens = TableQuery[EmailVerificationTokenTable]

  override def insert(
    emailVerificationToken: EmailVerificationToken
  )(implicit executionContext: ExecutionContext): Future[EmailVerificationToken] =
    db.run(emailVerificationTokens += emailVerificationToken)
      .flatMap { _ =>
        getByUserIdAndSecret(emailVerificationToken.userId, emailVerificationToken.secret) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getByUserIdAndSecret(
    userId: UUID, secret: UUID
  )(implicit executionContext: ExecutionContext): OptionT[Future, EmailVerificationToken] =
    OptionT {
      db.run {
          emailVerificationTokens
            .filter { emailVerificationToken =>
              emailVerificationToken.userId === userId && emailVerificationToken.secret === secret
            }
            .take(1)
            .result
        }
        .map(_.headOption)
    }

  override def verifyEmail(userId: UUID, secret: UUID)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, EmailVerificationToken] =
    getByUserIdAndSecret(userId, secret)
      .flatMap { result =>
        result.verifiedAt.fold {
          OptionT[Future, Int] {
            db.run {
                emailVerificationTokens
                  .filter { emailVerificationToken =>
                    emailVerificationToken.userId === userId && emailVerificationToken.secret === secret
                  }
                  .map(_.verifiedAt)
                  .update(Some(systemUtilities.currentTime()))
              }
              .map(Some.apply)
          }
            .flatMap { _ => verifyEmail(userId, secret) }
        }(_ => OptionT.some[Future, EmailVerificationToken](result))
      }

  override protected def initializeCommand()(implicit executionContext: ExecutionContext): Future[Unit] =
    db.run(emailVerificationTokens.schema.createIfNotExists)
}
