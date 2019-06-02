package modules

import com.google.inject.AbstractModule
import com.ruchij.shared.info.BuildInformation
import com.ruchij.shared.utils.SystemUtilities
import com.typesafe.config.{Config, ConfigFactory}

class MessageModule extends AbstractModule {
  override def configure(): Unit = {
    val config: Config = ConfigFactory.load()

    bind(classOf[SystemUtilities]).toInstance(SystemUtilities)
    bind(classOf[BuildInformation]).toInstance(BuildInformation.parse(config).get)
  }
}
