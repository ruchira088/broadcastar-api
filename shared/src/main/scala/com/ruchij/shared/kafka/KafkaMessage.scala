package com.ruchij.shared.kafka

case class KafkaMessage[A](value: A)(implicit val kafkaTopic: KafkaTopic[A])
