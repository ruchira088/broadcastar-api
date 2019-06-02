package web.controllers

import com.ruchij.shared.test.bindings.GuiceBinding._
import com.ruchij.shared.test.bindings.GuiceUtils.application
import com.ruchij.shared.test.utils.Matchers._
import com.ruchij.shared.utils.SystemUtilities
import info.BuildInfo
import org.joda.time.DateTime
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test._

import scala.util.Properties

class HomeControllerSpec extends PlaySpec {

  "HomeController GET /health" should {
    "return the health check JSON" in {

      val timestamp = DateTime.now()

      implicit val systemUtilities: SystemUtilities = new SystemUtilities {
        override def currentTime(): DateTime = timestamp
      }

      val app = application(classOf[SystemUtilities] -> systemUtilities)

      val request = FakeRequest(GET, "/health")
      val home = route(app, request).value

      status(home) mustBe OK
      contentType(home) must beJson
      contentAsJson(home) must equalJson {
        s"""
          {
            "serviceName": "${BuildInfo.name}",
            "version": "${BuildInfo.version}",
            "organization": "${BuildInfo.organization}",
            "javaVersion": "${Properties.javaVersion}",
            "sbtVersion": "${BuildInfo.sbtVersion}",
            "scalaVersion": "${BuildInfo.scalaVersion}",
            "osName": "${Properties.osName}",
            "timestamp": "${timestamp.toString}",
            "gitCommit": "unspecified",
            "gitBranch": "unspecified",
            "dockerBuildTimestamp": "1970-01-01T00:00:00.000Z"
          }
        """
      }

      await(app.stop())
    }
  }
}
