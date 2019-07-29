package com.ruchij.shared.kafka.admin

import com.ruchij.shared.ec.IOExecutionContext
import com.ruchij.shared.kafka.KafkaUtils.{commonClientProperties, toProperties}
import com.ruchij.shared.kafka.config.{KafkaClientConfiguration, KafkaTopicConfiguration}
import javax.inject.Inject
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.errors.{TopicExistsException, UnknownTopicOrPartitionException}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.reflectiveCalls

class
KafkaAdministratorImpl @Inject()(kafkaClientConfiguration: KafkaClientConfiguration, kafkaTopicConfiguration: KafkaTopicConfiguration, ioExecutionContext: IOExecutionContext) extends KafkaAdministrator {
  lazy val kafkaAdminClient: AdminClient =
    AdminClient.create { toProperties { commonClientProperties(kafkaClientConfiguration) } }

  override def createTopic(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    Future {
      kafkaAdminClient.createTopics {
        List(new NewTopic(topicName, kafkaTopicConfiguration.partitionCount, kafkaTopicConfiguration.replicationFactor.toShort))
          .asJavaCollection
      }
    }(ioExecutionContext)
      .flatMap {
        KafkaAdministratorImpl.handleResponse {
          promise => {
            case _: TopicExistsException => promise.success(false)
          }
        }
      }

  override def deleteTopic(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    Future {
      kafkaAdminClient.deleteTopics { List(topicName).asJavaCollection }
    }(ioExecutionContext)
      .flatMap {
        KafkaAdministratorImpl.handleResponse {
          promise => {
            case _: UnknownTopicOrPartitionException => promise.success(false)
          }
        }
      }

  override def listTopics()(implicit executionContext: ExecutionContext): Future[Set[String]] =
    Future {
      kafkaAdminClient.listTopics()
    }(ioExecutionContext)
    .flatMap {
      listTopicResult =>
        val promise = Promise[Set[String]]

        listTopicResult.names().whenComplete {
          (topics, throwable) =>
            Option(throwable).fold(promise.success(topics.asScala.toSet)) {
              _ => promise.failure(throwable)
            }
        }

        promise.future
    }

  override def close()(implicit executionContext: ExecutionContext): Future[Unit] =
    Future {
      kafkaAdminClient.close()
    }(ioExecutionContext)
}

object KafkaAdministratorImpl {
  def handleResponse(pf: Promise[Boolean] => PartialFunction[Throwable, Unit])(result: { def all(): KafkaFuture[Void] }): Future[Boolean] = {
    val promise = Promise[Boolean]

    result.all().whenComplete {
      (_: Void, throwable: Throwable) =>
        Option(throwable).fold[Unit](promise.success(true)) {
          pf(promise).orElse {
            case _ => promise.failure(throwable)
          }
        }
    }

    promise.future
  }
}
