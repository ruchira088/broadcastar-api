package com.ruchij.shared.kafka.schemaregistry

import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.exceptions.HttpResponseException
import com.ruchij.shared.utils.WsUtils.handleResponse
import javax.inject.{Inject, Singleton}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.ws.{WSAuthScheme, WSClient}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaRegistryImpl @Inject()(wsClient: WSClient, kafkaConfiguration: KafkaConfiguration) extends SchemaRegistry {
  override def removeSchema(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    wsClient
      .url(s"${kafkaConfiguration.schemaRegistryUrl}/subjects/$topicName")
      .withAuth(
        kafkaConfiguration.schemaRegistryUsername,
        kafkaConfiguration.schemaRegistryPassword,
        WSAuthScheme.BASIC
      )
      .execute(HttpVerbs.DELETE)
      .flatMap(handleResponse[List[Int]])
      .map(_ => true)
      .recover {
        case HttpResponseException(Status.NOT_FOUND, _, _) => false
      }

  override def listSchemas()(implicit executionContext: ExecutionContext): Future[List[String]] =
    wsClient
      .url(s"${kafkaConfiguration.schemaRegistryUrl}/subjects")
      .withAuth(
        kafkaConfiguration.schemaRegistryUsername,
        kafkaConfiguration.schemaRegistryPassword,
        WSAuthScheme.BASIC
      )
      .execute(HttpVerbs.GET)
      .flatMap(handleResponse[List[String]])
}
