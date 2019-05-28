package config

import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class ApplicationConfiguration(
  applicationInformation: ApplicationInformation,
  authenticationConfiguration: AuthenticationConfiguration,
  localFileStoreConfiguration: LocalFileStoreConfiguration,
  s3Configuration: S3Configuration
)

object ApplicationConfiguration {
  implicit val applicationConfigurationWrites: OWrites[ApplicationConfiguration] = Json.writes[ApplicationConfiguration]

  def parse(config: Config): Try[ApplicationConfiguration] =
    for {
      applicationInformation <- ApplicationInformation.parse(config)
      authenticationConfiguration <- AuthenticationConfiguration.parse(config)
      localFileStoreConfiguration <- LocalFileStoreConfiguration.parse(config)
      s3Configuration <- S3Configuration.parse(config)
    } yield
      ApplicationConfiguration(
        applicationInformation,
        authenticationConfiguration,
        localFileStoreConfiguration,
        s3Configuration
      )
}
