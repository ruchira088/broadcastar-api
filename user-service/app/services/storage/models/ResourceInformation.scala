package services.storage.models

import org.joda.time.DateTime

case class ResourceInformation(
  key: String,
  createdAt: DateTime,
  fileName: String,
  contentType: Option[String],
  fileSize: Long
)
