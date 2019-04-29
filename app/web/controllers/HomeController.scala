package web.controllers

import config.SystemUtilities
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import web.responses.models.HealthCheck

@Singleton
class HomeController @Inject()(controllerComponents: ControllerComponents)(implicit systemUtilities: SystemUtilities)
    extends AbstractController(controllerComponents) {

  def healthCheck(): Action[AnyContent] =
    Action {
      Ok {
        Json.toJson {
          HealthCheck()
        }
      }
    }
}
