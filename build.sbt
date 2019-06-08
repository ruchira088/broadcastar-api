import Dependencies._

inThisBuild {
  Seq(
    organization := "com.ruchij",
    scalaVersion := SCALA_VERSION,
    scalacOptions += "-feature"
  )
}

lazy val userService =
  (project in file("./user-service"))
    .enablePlugins(PlayScala, BuildInfoPlugin)
    .settings(
      name := "user-service",
      version := "0.0.1",
      maintainer := "me@ruchij.com",
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "info",
      libraryDependencies ++=
        Seq(guice, scalaz, jodaTime, playSlick, postgresql, sqlite, h2, jbcrypt, s3),
      libraryDependencies ++=
        Seq(scalaTestPlusPlay, pegdown, faker).map(_ % Test),
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-h", "user-service/target/test-results/unit-tests"),
      javaOptions in Test += "-Dconfig.file=conf/application.test.conf",
      envVars in Test :=
        Map(
          "GIT_COMMIT" -> "unspecified",
          "GIT_BRANCH" -> "unspecified",
          "DOCKER_BUILD_TIMESTAMP" -> "1970-01-01T00:00:00Z"
        )
    )
    .dependsOn(macros, shared % "compile->compile;test->test")

lazy val messageService =
  (project in file("./message-service"))
    .enablePlugins(PlayScala, BuildInfoPlugin)
    .settings(
      name := "message-service",
      version := "0.0.1",
      maintainer := "me@ruchij.com",
      buildInfoKeys := BuildInfoKey.ofN(name, organization, version, scalaVersion, sbtVersion),
      buildInfoPackage := "info",
      libraryDependencies ++=
        Seq(guice, jodaTime)
    )
    .dependsOn(shared % "compile->compile;test->test")

lazy val shared =
  (project in file("./shared"))
    .enablePlugins(PlayScala)
    .disablePlugins(PlayLayoutPlugin)
    .settings(
      name := "shared",
      version := "0.0.1",
      libraryDependencies ++= Seq(ws, scalaz, jodaTime, commonsValidator),
      libraryDependencies ++= Seq(scalaTestPlusPlay).map(_ % Test)
    )
    .dependsOn(macros)

lazy val macros =
  (project in file("./macros"))
    .settings(
      name := "macros",
      version := "0.0.1",
      libraryDependencies ++= Seq(scalaReflect, typesafeConfig, jodaTime)
    )

addCommandAlias("cleanAll", "; messageService/clean; userService/clean; shared/clean; macros/clean")
addCommandAlias("compileAll", "; macros/compile; shared/compile; userService/compile; messageService/compile")
addCommandAlias("testWithCoverage", "; coverage; userService/test; messageService/test; coverageReport")

addCommandAlias("userServiceWithPostgresql", "userService/run -Dconfig.file=user-service/conf/application.postgresql.conf")
addCommandAlias("userServiceWithSqlite", "userService/run -Dconfig.file=user-service/conf/application.sqlite.conf")
addCommandAlias("userServiceWithH2", "userService/run -Dconfig.file=user-service/conf/application.h2.conf")
