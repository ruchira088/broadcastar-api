package web.controllers

import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.responses.models.ServiceInformation
import info.BuildInfo
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import services.background.BackgroundService

import scala.util.Properties

@Singleton
class HomeController @Inject()(backgroundService: BackgroundService, controllerComponents: ControllerComponents)(
  implicit systemUtilities: SystemUtilities,
  applicationInformation: BuildInformation
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
            applicationInformation.gitCommit,
            applicationInformation.gitBranch,
            applicationInformation.dockerBuildTimestamp
          )
        }
      }
    }
}
