package config

import scala.concurrent.duration.FiniteDuration

case class AuthenticationConfiguration(sessionDuration: FiniteDuration, passwordResetTokenDuration: FiniteDuration)
