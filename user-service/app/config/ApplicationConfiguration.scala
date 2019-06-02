package config

import com.ruchij.shared.info.BuildInformation
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class ApplicationConfiguration(
  applicationInformation: BuildInformation,
  authenticationConfiguration: AuthenticationConfiguration,
  localFileStoreConfiguration: LocalFileStoreConfiguration,
  s3Configuration: S3Configuration
)

object ApplicationConfiguration {
  implicit val applicationConfigurationWrites: OWrites[ApplicationConfiguration] = Json.writes[ApplicationConfiguration]

  def parse(config: Config): Try[ApplicationConfiguration] =
    for {
      buildInformation <- BuildInformation.parse(config)
      authenticationConfiguration <- AuthenticationConfiguration.parse(config)
      localFileStoreConfiguration <- LocalFileStoreConfiguration.parse(config)
      s3Configuration <- S3Configuration.parse(config)
    } yield
      ApplicationConfiguration(
        buildInformation,
        authenticationConfiguration,
        localFileStoreConfiguration,
        s3Configuration
      )
}