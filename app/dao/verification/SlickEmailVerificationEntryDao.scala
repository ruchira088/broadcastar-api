package dao.verification

import java.util.UUID

import dao.InitializableTable
import dao.verification.models.EmailVerificationEntry
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import utils.SystemUtilities
import utils.MonadicUtils.OptionTWrapper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickEmailVerificationEntryDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit systemUtilities: SystemUtilities
) extends HasDatabaseConfigProvider[JdbcProfile]
    with EmailVerificationEntryDao
    with InitializableTable {

  import dbConfig.profile.api._
  import dao.SlickMappedColumns.dateTimeMappedColumn

  override val TABLE_NAME: String = "email_verification_entries"

  class EmailVerificationEntryTable(tag: Tag) extends Table[EmailVerificationEntry](tag, TABLE_NAME) {
    def userId: Rep[UUID] = column[UUID]("user_id")
    def verificationToken: Rep[UUID] = column[UUID]("verification_token")
    def email: Rep[String] = column[String]("email")
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def verifiedAt: Rep[Option[DateTime]] = column[Option[DateTime]]("verified_at")

    def pk = primaryKey("user_id_and_verification_token", (userId, verificationToken))

    override def * : ProvenShape[EmailVerificationEntry] =
      (userId, verificationToken, email, createdAt, verifiedAt) <> (EmailVerificationEntry.apply _ tupled, EmailVerificationEntry.unapply)
  }

  val emailVerificationEntries = TableQuery[EmailVerificationEntryTable]

  override def insert(
    emailVerificationEntry: EmailVerificationEntry
  )(implicit executionContext: ExecutionContext): Future[EmailVerificationEntry] =
    db.run(emailVerificationEntries += emailVerificationEntry)
      .flatMap {
        _ => getByUserIdAndVerificationToken(emailVerificationEntry.userId, emailVerificationEntry.verificationToken) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getByUserIdAndVerificationToken(userId: UUID, verificationToken: UUID)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, EmailVerificationEntry] =
    OptionT {
      db.run {
          emailVerificationEntries
            .filter { emailVerificationEntry =>
              emailVerificationEntry.userId === userId && emailVerificationEntry.verificationToken === verificationToken
            }
            .take(1)
            .result
        }
        .map(_.headOption)
    }

  override def verifyEmail(userId: UUID, verificationToken: UUID)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, EmailVerificationEntry] =
    getByUserIdAndVerificationToken(userId, verificationToken)
      .flatMap { result =>
        result.verifiedAt.fold {
          OptionT {
            db.run {
                emailVerificationEntries
                  .filter { emailVerificationEntry =>
                    emailVerificationEntry.userId === userId && emailVerificationEntry.verificationToken === verificationToken
                  }
                  .map(_.verifiedAt)
                  .update(Some(systemUtilities.currentTime()))
              }
              .flatMap { _ =>
                getByUserIdAndVerificationToken(userId, verificationToken).run
              }
          }
        }(_ => OptionT.some[Future, EmailVerificationEntry](result))
      }

  override protected def initializeCommand()(implicit executionContext: ExecutionContext): Future[Unit] =
    db.run(emailVerificationEntries.schema.createIfNotExists)
}
