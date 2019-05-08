package services.storage.store
import config.S3Configuration
import javax.inject.{Inject, Singleton}
import scalaz.OptionT
import software.amazon.awssdk.core.internal.async.{ByteArrayAsyncRequestBody, ByteArrayAsyncResponseTransformer}
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse, HeadObjectRequest, ListObjectsRequest, PutObjectRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.compat.java8.FutureConverters.toScala

@Singleton
class S3FileStore @Inject()(s3AsyncClient: S3AsyncClient, s3Configuration: S3Configuration) extends FileStore {

  override def read(key: String)(implicit executionContext: ExecutionContext): OptionT[Future, Array[Byte]] =
    OptionT[Future, Array[Byte]] {
      toScala {
        s3AsyncClient.getObject(
          GetObjectRequest.builder().bucket(s3Configuration.s3Bucket).key(key).build(),
          new ByteArrayAsyncResponseTransformer[GetObjectResponse]
        )
      }
        .map(result => Some(result.asByteArray()))
    }

  override def write(key: String, data: Array[Byte])(implicit executionContext: ExecutionContext): Future[FullPath] =
    toScala {
      s3AsyncClient.putObject(
        PutObjectRequest.builder().bucket(s3Configuration.s3Bucket).key(key).build(),
        new ByteArrayAsyncRequestBody(data)
      )
    }
      .map(_ => s"${s3Configuration.s3Bucket}/$key")

}
