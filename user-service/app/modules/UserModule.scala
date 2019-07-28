package modules

import play.api.{Configuration, Environment, Mode}
import play.api.inject.{Binding, Module}
import UserServiceModule._

class UserModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] =
    unsafeBindings(configuration.underlying)(
      coreConfigurationBindings,
      if (environment.mode == Mode.Prod) onlineBindings else localBindings
    ) ++ serviceBindings ++ backgroundBindings ++ daoBindings ++ executionContextBindings ++ other
}
