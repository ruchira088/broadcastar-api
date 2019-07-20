import sbt._

object Dependencies {
  val SCALA_VERSION = "2.12.8"

  lazy val scalaReflect = "org.scala-lang" % "scala-reflect" % SCALA_VERSION

  lazy val jodaTime = "joda-time" % "joda-time" % "2.10.3"

  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.28"

  lazy val playSlick = "com.typesafe.play" %% "play-slick" % "4.0.2"

  lazy val playSlickEvolutions = "com.typesafe.play" %% "play-slick-evolutions" % "4.0.2"

  lazy val postgresql = "org.postgresql" % "postgresql" % "42.2.6"

  lazy val sqlite = "org.xerial" % "sqlite-jdbc" % "3.28.0"

  lazy val h2 = "com.h2database" % "h2" % "1.4.199"

  lazy val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val commonsValidator = "commons-validator" % "commons-validator" % "1.6"

  lazy val s3 = "software.amazon.awssdk" % "s3" % "2.7.8"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.3.4"

  lazy val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.5.23"

  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.23"

  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "2.3.0"

  lazy val kafkaAvroSerializer = "io.confluent" % "kafka-avro-serializer" % "5.3.0"

  lazy val avro4s = "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.4"

  lazy val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.4"
  
  lazy val sendgrid = "com.sendgrid" % "sendgrid-java" % "4.4.1"

  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  lazy val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"

  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"

  lazy val faker = "com.github.javafaker" % "javafaker" % "1.0.0"
}
