package com.ruchij.shared.kafka.admin

import com.ruchij.shared.config.KafkaConfiguration
import com.ruchij.shared.ec.IOExecutionContext
import com.ruchij.shared.kafka.KafkaUtils.{commonClientProperties, toProperties}
import javax.inject.Inject
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.common.errors.TopicExistsException

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future, Promise}

class KafkaAdministratorImpl @Inject()(kafkaConfiguration: KafkaConfiguration, ioExecutionContext: IOExecutionContext) extends KafkaAdministrator {
  lazy val kafkaAdminClient: AdminClient =
    AdminClient.create { toProperties { commonClientProperties(kafkaConfiguration) } }

  override def createTopic(topicName: String)(implicit executionContext: ExecutionContext): Future[Boolean] =
    Future {
      kafkaAdminClient.createTopics {
        List(new NewTopic(topicName, kafkaConfiguration.topicPartitionCount, kafkaConfiguration.topicReplicationFactor.toShort))
          .asJavaCollection
      }
    }(ioExecutionContext)
      .flatMap {
        createTopicsResult => {
          val promise = Promise[Boolean]

          createTopicsResult.all().whenComplete {
            (_: Void, throwable: Throwable) =>
              Option(throwable).fold(promise.success(true)) {
                case _: TopicExistsException => promise.success(false)
                case _ => promise.failure(throwable)
              }
          }

          promise.future
        }
      }

  override def close()(implicit executionContext: ExecutionContext): Future[Unit] =
    Future {
      kafkaAdminClient.close()
    }(ioExecutionContext)
}
