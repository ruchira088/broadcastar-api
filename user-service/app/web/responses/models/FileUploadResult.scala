package web.responses.models

import play.api.libs.json.{Json, OWrites}

case class FileUploadResult(
  parameterName: String,
  key: String,
  fileName: String,
  fileSize: Long,
  contentType: Option[String]
)

object FileUploadResult {
  implicit val fileUploadResultWrites: OWrites[FileUploadResult] = Json.writes[FileUploadResult]
}
