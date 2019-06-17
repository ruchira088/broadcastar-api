package dao.authentication

import java.util.UUID

import com.ruchij.shared.utils.MonadicUtils.OptionTWrapper
import dao.authentication.model.SlickAuthenticationToken
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import services.authentication.models.AuthenticationToken
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickAuthenticationTokenDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with AuthenticationTokenDao {

  import dao.SlickMappedColumns.dateTimeMappedColumn
  import dbConfig.profile.api._

  class AuthenticationTokenDao(tag: Tag) extends Table[SlickAuthenticationToken](tag, SlickAuthenticationTokenDao.TABLE_NAME) {
    def sessionToken: Rep[String] = column[String]("session_token", O.PrimaryKey)
    def userId: Rep[UUID] = column[UUID]("user_id")
    def secretToken: Rep[UUID] = column[UUID]("secret_token")
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def expiresAt: Rep[DateTime] = column[DateTime]("expires_at")

    override def * : ProvenShape[SlickAuthenticationToken] =
      (sessionToken, userId, secretToken, createdAt, expiresAt) <> (SlickAuthenticationToken.apply _ tupled, SlickAuthenticationToken.unapply)
  }

  val authenticationTokens = TableQuery[AuthenticationTokenDao]

  override def insert(
    authenticationToken: AuthenticationToken
  )(implicit executionContext: ExecutionContext): Future[AuthenticationToken] =
    db.run(authenticationTokens += SlickAuthenticationToken.fromAuthenticationToken(authenticationToken))
      .flatMap {
        _ => getBySessionToken(AuthenticationToken.sessionToken(authenticationToken)) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getBySessionToken(sessionToken: String)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, AuthenticationToken] =
    OptionT[Future, AuthenticationToken] {
      db.run { authenticationTokens.filter(_.sessionToken === sessionToken).take(1).result }
        .map { _.headOption.map(SlickAuthenticationToken.toAuthenticationToken) }
    }

  override def update(sessionToken: String, authenticationToken: AuthenticationToken)(implicit executionContext: ExecutionContext): OptionT[Future, AuthenticationToken] =
    getBySessionToken(sessionToken)
      .flatMapF {
        _ => db.run {
          authenticationTokens
            .filter(_.sessionToken === sessionToken)
            .update(SlickAuthenticationToken.fromAuthenticationToken(authenticationToken))
        }
      }
      .flatMap { _ => getBySessionToken(AuthenticationToken.sessionToken(authenticationToken)) }
}

object SlickAuthenticationTokenDao {
  val TABLE_NAME = "authentication_tokens"
}
