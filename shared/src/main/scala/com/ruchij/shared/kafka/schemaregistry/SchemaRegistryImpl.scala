package com.ruchij.shared.kafka.schemaregistry

import com.ruchij.shared.exceptions.HttpResponseException
import com.ruchij.shared.kafka.config.KafkaClientConfiguration
import com.ruchij.shared.kafka.config.KafkaClientConfiguration.{ConfluentKafkaClientConfiguration, LocalKafkaClientConfiguration}
import com.ruchij.shared.utils.WsUtils.handleResponse
import javax.inject.{Inject, Singleton}
import play.api.http.{HttpVerbs, Status}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaRegistryImpl @Inject()(wsClient: WSClient, kafkaClientConfiguration: KafkaClientConfiguration) extends SchemaRegistry {
  override def removeSchema(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    SchemaRegistryImpl.wsRequest(wsClient, s"subject/$topicName")(kafkaClientConfiguration)
      .execute(HttpVerbs.DELETE)
      .flatMap(handleResponse[List[Int]])
      .map(_ => true)
      .recover {
        case HttpResponseException(Status.NOT_FOUND, _, _) => false
      }

  override def listSchemas()(implicit executionContext: ExecutionContext): Future[List[String]] =
    SchemaRegistryImpl.wsRequest(wsClient, "subject")(kafkaClientConfiguration)
      .execute(HttpVerbs.GET)
      .flatMap(handleResponse[List[String]])
}

object SchemaRegistryImpl {
  def wsRequest(wsClient: WSClient, path: String): PartialFunction[KafkaClientConfiguration, WSRequest] = {
    case LocalKafkaClientConfiguration(_, schemaRegistryUrl, _) =>
      wsClient.url(s"$schemaRegistryUrl/$path")

    case ConfluentKafkaClientConfiguration(_, schemaRegistryUrl, _, _, _, schemaRegistryUsername, schemaRegistryPassword) =>
      wsClient.url(s"$schemaRegistryUrl/$path").withAuth(schemaRegistryUsername.value, schemaRegistryPassword.value, WSAuthScheme.BASIC)
  }
}