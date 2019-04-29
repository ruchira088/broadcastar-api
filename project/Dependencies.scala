import sbt._

object Dependencies {
  val SCALA_VERSION = "2.12.8"

  lazy val jodaTime = "joda-time" % "joda-time" % "2.10.1"
  
  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.27"
  
  lazy val playSlick = "com.typesafe.play" %% "play-slick" % "4.0.1"
  
  lazy val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2"
  
  lazy val pegdown = "org.pegdown" % "pegdown" % "1.6.0"
}
