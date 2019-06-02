package web.controllers

import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.responses.models.ServiceInformation
import info.BuildInfo
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.util.Properties

@Singleton
class HomeController @Inject()(controllerComponents: ControllerComponents)(
  implicit systemUtilities: SystemUtilities,
  buildInformation: BuildInformation
) extends AbstractController(controllerComponents) {

  def healthCheck(): Action[AnyContent] =
    Action {
      Ok {
        Json.toJson {
          ServiceInformation(
            BuildInfo.name,
            BuildInfo.organization,
            BuildInfo.version,
            Properties.javaVersion,
            BuildInfo.sbtVersion,
            BuildInfo.scalaVersion,
            systemUtilities.currentTime(),
            Properties.osName,
            buildInformation.gitCommit,
            buildInformation.gitBranch,
            buildInformation.dockerBuildTimestamp
          )
        }
      }
    }

}
