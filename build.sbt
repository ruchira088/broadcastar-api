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
      libraryDependencies ++= rootDependencies ++ rootTestDependencies.map(_ % Test),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-results/unit-tests"),
      javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
    )

lazy val rootDependencies = Seq(guice, scalaz, jodaTime, playSlick, postgresql, sqlite, jbcrypt, commonsValidator)

lazy val rootTestDependencies = Seq(scalaTestPlusPlay, h2, pegdown)

addCommandAlias("runWithPostgresql", "run -Dconfig.file=conf/application.postgresql.conf")
addCommandAlias("runWithSqlite", "run -Dconfig.file=conf/application.sqlite.conf")
addCommandAlias("runWithH2", "run -Dconfig.file=conf/application.h2.conf")
