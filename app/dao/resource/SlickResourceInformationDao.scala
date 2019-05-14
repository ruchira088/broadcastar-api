package dao.resource

import dao.InitializableTable
import exceptions.FatalDatabaseException
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import scalaz.OptionT
import scalaz.std.scalaFuture.futureInstance
import services.storage.models.ResourceInformation
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape
import utils.MonadicUtils.OptionTWrapper

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

@Singleton
class SlickResourceInformationDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)
    extends HasDatabaseConfigProvider[JdbcProfile]
    with ResourceInformationDao with InitializableTable {

  import dbConfig.profile.api._
  import dao.SlickMappedColumns.dateTimeMappedColumn

  override val TABLE_NAME: String = "resource_information"

  class ResourceInformationTable(tag: Tag)
      extends Table[ResourceInformation](tag, TABLE_NAME) {
    def key: Rep[String] = column[String]("key", O.PrimaryKey)
    def createdAt: Rep[DateTime] = column[DateTime]("created_at")
    def fileName: Rep[String] = column[String]("file_name")
    def contentType: Rep[Option[String]] = column[Option[String]]("content_type")
    def fileSize: Rep[Long] = column[Long]("file_size")

    override def * : ProvenShape[ResourceInformation] =
      (key, createdAt, fileName, contentType, fileSize) <> (ResourceInformation.apply _ tupled, ResourceInformation.unapply)
  }

  val resourceInformationItems = TableQuery[ResourceInformationTable]

  override def insert(resourceInformation: ResourceInformation)(
    implicit executionContext: ExecutionContext
  ): Future[ResourceInformation] =
    db.run(resourceInformationItems += resourceInformation)
      .flatMap {
        _ => getByKey(resourceInformation.key) ifEmpty Future.failed(FatalDatabaseException)
      }

  override def getByKey(key: String)(
    implicit executionContext: ExecutionContext
  ): OptionT[Future, ResourceInformation] =
    OptionT {
      db.run { resourceInformationItems.filter(_.key === key).take(1).result }
        .map(_.headOption)
    }

  override protected def initializeCommand()(implicit executionContext: ExecutionContext): Future[Unit] =
    db.run(resourceInformationItems.schema.createIfNotExists)
}
