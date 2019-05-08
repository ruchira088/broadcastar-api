package dao.resource

import scalaz.OptionT
import services.storage.models.ResourceInformation

import scala.concurrent.{ExecutionContext, Future}

trait ResourceInformationDao {
  def insert(resourceInformation: ResourceInformation)(implicit executionContext: ExecutionContext): Future[ResourceInformation]

  def getByKey(key: String)(implicit executionContext: ExecutionContext): OptionT[Future, ResourceInformation]
}
