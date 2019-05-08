package web.controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import utils.SystemUtilities
import web.responses.models.HealthCheckResponse

@Singleton
class HomeController @Inject()(controllerComponents: ControllerComponents)(implicit systemUtilities: SystemUtilities)
    extends AbstractController(controllerComponents) {

  def healthCheck(): Action[AnyContent] =
    Action {
      Ok { Json.toJson { HealthCheckResponse() } }
    }
}
