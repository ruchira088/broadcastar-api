package com.ruchij.shared.kafka

import java.util.Properties

import com.ruchij.shared.kafka.config.KafkaClientConfiguration
import com.ruchij.shared.kafka.config.KafkaClientConfiguration.{ConfluentKafkaClientConfiguration, LocalKafkaClientConfiguration}
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.{SaslConfigs, SslConfigs}
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.security.plain.PlainLoginModule
import org.apache.kafka.common.security.plain.internals.PlainSaslServer

object KafkaUtils {
  def commonClientProperties: PartialFunction[KafkaClientConfiguration, Map[String, String]] = {
    case LocalKafkaClientConfiguration(bootstrapServers, _, _) =>
      Map(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> bootstrapServers)

    case confluentKafkaClientConfiguration: ConfluentKafkaClientConfiguration =>
      Map(
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> SecurityProtocol.SASL_SSL.name,
        SaslConfigs.SASL_MECHANISM -> PlainSaslServer.PLAIN_MECHANISM,
        SaslConfigs.SASL_JAAS_CONFIG ->
          s"""${classOf[PlainLoginModule].getName} required username="${confluentKafkaClientConfiguration.kafkaUsername.value}" password="${confluentKafkaClientConfiguration.kafkaPassword.value}";""",
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG -> SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> confluentKafkaClientConfiguration.bootstrapServers
      )
  }

  def schemaRegistryConfiguration: PartialFunction[KafkaClientConfiguration, Map[String, String]] = {
    case LocalKafkaClientConfiguration(_, schemaRegistryUrl, _) =>
      Map(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl)

    case confluentKafkaClientConfiguration: ConfluentKafkaClientConfiguration =>
      Map(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> confluentKafkaClientConfiguration.schemaRegistryUrl,
        SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE -> "USER_INFO",
        SchemaRegistryClientConfig.USER_INFO_CONFIG -> s"${confluentKafkaClientConfiguration.schemaRegistryUsername.value}:${confluentKafkaClientConfiguration.schemaRegistryPassword.value}"
      )
  }

  def toProperties(map: Map[String, String]): Properties =
    new Properties() {
      map.foreach {
        case (key, value) => setProperty(key, value)
      }
    }
}
