package modules

import com.ruchij.shared.config.DevelopmentConfiguration
import com.ruchij.shared.config.models.DevelopmentMode
import com.ruchij.shared.config.models.DevelopmentMode.{DockerCompose, Lean, Local, Online}
import com.ruchij.shared.monads.MonadicUtils.tryMonadError
import com.typesafe.scalalogging.Logger
import modules.UserServiceModule._
import play.api.Mode.{Dev, Prod, Test}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment, Mode}

class UserModule extends Module {
  private val logger = Logger[UserModule]

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    unsafeBindingsRun(configuration.underlying)(
      coreConfigurationBindings,
      developmentConfiguration.flatMap {
        case DevelopmentConfiguration(developmentMode) =>
          externalBindings(environment.mode, developmentMode)
      },
    ) ++ serviceBindings ++ backgroundBindings ++ daoBindings ++ executionContextBindings ++ other

  val externalBindings: PartialFunction[(Mode, DevelopmentMode), UnsafeBindings] = {
    case (Test, _) | (Dev, Lean) =>
      logger.info("Lean bindings are used.")
      leanBindings

    case (Dev, Local) =>
      logger.info("Local bindings are used")
      localBindings

    case (Dev, DockerCompose) =>
      logger.info("Docker Compose bindings are used.")
      dockerComposeBindings

    case (Prod, _) | (Dev, Online) =>
      logger.info("Online bindings are used.")
      onlineBindings
  }
}
