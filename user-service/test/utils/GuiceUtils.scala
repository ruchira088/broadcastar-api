package utils

import com.ruchij.shared.test.bindings.GuiceBinding
import com.ruchij.shared.test.bindings.GuiceBinding.GuiceClassBinding
import com.ruchij.shared.test.bindings.GuiceUtils.{application => app}
import play.api.Application
import services.background.BackgroundService
import stubs.services.StubBackgroundService

object GuiceUtils {
  def application(guiceBinding: GuiceBinding[_]*): Application =
    app(guiceBinding :+ GuiceClassBinding(classOf[BackgroundService], classOf[StubBackgroundService]): _*)
}
