package com.ruchij.shared.kafka.stubs.models

import com.ruchij.shared.kafka.KafkaTopic
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.RecordMetadata

case class StubMessage[A](kafkaTopic: KafkaTopic[A], genericRecord: GenericRecord, recordMetadata: RecordMetadata)
