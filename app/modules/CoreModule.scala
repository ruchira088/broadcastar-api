package modules

import com.google.inject.AbstractModule
import config.SystemUtilities

class CoreModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
  }
}
