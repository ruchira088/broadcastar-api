package config

import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.info.BuildInformation
import com.typesafe.config.Config
import play.api.libs.json.{Json, OWrites}

import scala.util.Try

case class ApplicationConfiguration(
  applicationInformation: BuildInformation,
  authenticationConfiguration: SessionConfiguration,
  localFileStoreConfiguration: LocalFileStoreConfiguration,
  s3Configuration: S3Configuration,
  kafkaConfiguration: KafkaConfiguration
)

object ApplicationConfiguration {
  implicit val applicationConfigurationWrites: OWrites[ApplicationConfiguration] = Json.writes[ApplicationConfiguration]

  def parse(config: Config): Try[ApplicationConfiguration] =
    for {
      buildInformation <- BuildInformation.parse(config)
      sessionConfiguration <- SessionConfiguration.parse(config)
      localFileStoreConfiguration <- LocalFileStoreConfiguration.parse(config)
      s3Configuration <- S3Configuration.parse(config)
      kafkaConfiguration <- KafkaConfiguration.parse(config)
    } yield
      ApplicationConfiguration(
        buildInformation,
        sessionConfiguration,
        localFileStoreConfiguration,
        s3Configuration,
        kafkaConfiguration
      )
}
