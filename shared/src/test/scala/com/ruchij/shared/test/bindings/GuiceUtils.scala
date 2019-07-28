package com.ruchij.shared.test.bindings

import play.api.Application
import play.api.inject.Binding
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

object GuiceUtils {
  def application(guiceableModules: List[GuiceableModule], bindings: Binding[_]*): Application =
    GuiceApplicationBuilder()
      .bindings(guiceableModules: _*)
      .overrides(bindings)
      .build()

  def application(bindings: Binding[_]*): Application =
    application(List.empty, bindings: _*)
}
