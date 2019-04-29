package bindings
import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

object GuiceUtils {
  def application(guiceableModules: List[GuiceableModule], guiceBindings: GuiceBinding[_]*): Application =
    GuiceApplicationBuilder()
      .bindings(guiceableModules: _*)
      .overrides(guiceBindings.map(_.bindClazz()))
      .build()

  def application(guiceBindings: GuiceBinding[_]*): Application =
    application(List.empty, guiceBindings: _*)
}
