import Dependencies._

lazy val root =
  (project in file("."))
    .enablePlugins(PlayScala, BuildInfoPlugin)
    .settings(
      name := "broadcastar-api",
      organization := "com.ruchij",
      version := "0.0.1",
      maintainer := "ruchira088@gmail.com",
      scalaVersion := SCALA_VERSION,
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "com.eed3si9n.ruchij",
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-results/unit-tests"),
      libraryDependencies ++= rootDependencies ++ rootTestDependencies.map(_ % Test)
    )

lazy val rootDependencies = Seq(guice, scalaz, jodaTime, playSlick, jbcrypt)

lazy val rootTestDependencies = Seq(scalaTestPlusPlay, pegdown)