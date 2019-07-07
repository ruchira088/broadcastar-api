package com.ruchij.shared.kafka

import java.util.Properties

import com.ruchij.shared.config.KafkaConfiguration
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.{SaslConfigs, SslConfigs}
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.security.plain.PlainLoginModule
import org.apache.kafka.common.security.plain.internals.PlainSaslServer

object KafkaUtils {
  def commonClientProperties(kafkaConfiguration: KafkaConfiguration): Map[String, String] =
    Map(
      CommonClientConfigs.SECURITY_PROTOCOL_CONFIG -> SecurityProtocol.SASL_SSL.name,
      SaslConfigs.SASL_MECHANISM -> PlainSaslServer.PLAIN_MECHANISM,
      SaslConfigs.SASL_JAAS_CONFIG ->
        s"""${classOf[PlainLoginModule].getName} required username="${kafkaConfiguration.kafkaUsername}" password="${kafkaConfiguration.kafkaPassword}";""",
      SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG -> SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
      CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG -> kafkaConfiguration.bootstrapServers
    )

  def schemaRegistryConfiguration(kafkaConfiguration: KafkaConfiguration): Map[String, String] =
    Map(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> kafkaConfiguration.schemaRegistryUrl,
      SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE -> "USER_INFO",
      SchemaRegistryClientConfig.USER_INFO_CONFIG -> s"${kafkaConfiguration.schemaRegistryUsername}:${kafkaConfiguration.schemaRegistryPassword}"
    )

  def toProperties(map: Map[String, String]): Properties =
    new Properties() {
      map.foreach {
        case (key, value) => setProperty(key, value)
      }
    }
}
