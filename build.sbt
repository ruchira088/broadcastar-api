import Dependencies._

lazy val commonSettings =
  Seq(
    organization := "com.ruchij",
    maintainer := "me@ruchij.com",
    scalaVersion := SCALA_VERSION
  )

lazy val root =
  (project in file("."))
    .enablePlugins(PlayScala, BuildInfoPlugin)
    .settings(
      name := "chirper-api",
      version := "0.0.1",
      commonSettings,
      scalacOptions ++= Seq("-feature"),
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "info",
      libraryDependencies ++= rootDependencies ++ rootTestDependencies.map(_ % Test),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-results/unit-tests"),
      javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
    )
    .dependsOn(macroUtils)

lazy val macroUtils =
  (project in file("./macro-utils"))
    .settings(
      name := "macro-utils",
      version := "0.0.1",
      commonSettings,
      libraryDependencies ++= Seq(scalaReflect, typesafeConfig, jodaTime)
    )

lazy val rootDependencies =
  Seq(guice, scalaz, jodaTime, playSlick, postgresql, sqlite, h2, jbcrypt, commonsValidator, s3)

lazy val rootTestDependencies = Seq(scalaTestPlusPlay, pegdown, faker)

addCommandAlias("runWithPostgresql", "run -Dconfig.file=conf/application.postgresql.conf")
addCommandAlias("runWithSqlite", "run -Dconfig.file=conf/application.sqlite.conf")
addCommandAlias("runWithH2", "run -Dconfig.file=conf/application.h2.conf")
