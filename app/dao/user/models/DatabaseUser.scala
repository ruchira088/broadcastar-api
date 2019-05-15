package dao.user.models

import java.util.UUID

import org.joda.time.DateTime
import services.user.models.User
import utils.SystemUtilities
import web.requests.models.CreateUserRequest

case class DatabaseUser(
  userId: UUID,
  createdAt: DateTime,
  username: String,
  firstName: String,
  lastName: Option[String],
  email: String,
  password: String,
  emailVerified: Boolean
)

object DatabaseUser {
  def from(createUserRequest: CreateUserRequest, saltedHashedPassword: String)(
    implicit systemUtilities: SystemUtilities
  ): DatabaseUser =
    DatabaseUser(
      systemUtilities.randomUuid(),
      systemUtilities.currentTime(),
      createUserRequest.username,
      createUserRequest.firstName,
      createUserRequest.lastName.flatMap(value => if (value.trim.isEmpty) None else Some(value)),
      createUserRequest.email,
      saltedHashedPassword,
      emailVerified = false
    )

  def toUser(databaseUser: DatabaseUser): User =
    User(
      databaseUser.userId,
      databaseUser.createdAt,
      databaseUser.username,
      databaseUser.firstName,
      databaseUser.lastName,
      databaseUser.email
    )
}
