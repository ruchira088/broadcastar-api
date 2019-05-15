package services.user

import bindings.GuiceUtils.application
import org.scalatest.{AsyncFlatSpec, MustMatchers}
import utils.RandomGenerator
import web.requests.models.CreateUserRequest

import scala.concurrent.Future

class UserServiceImplSpec extends AsyncFlatSpec with MustMatchers {
  "UserServiceImpl" should "handle user related operations" in {

    val app = application()
    val userService = app.injector.instanceOf[UserServiceImpl]

    val createUserRequest = RandomGenerator.generate[CreateUserRequest]

    for {
      user <- userService.createUser(createUserRequest)

      _ = {
        user.firstName mustBe createUserRequest.firstName
        user.lastName mustBe createUserRequest.lastName
        user.username mustBe createUserRequest.username
        user.email mustBe createUserRequest.email
        user.profileImageId mustBe createUserRequest.profileImageId
      }

      otherUsers <-
        Future.sequence {
          List.fill(10)(RandomGenerator.generate[CreateUserRequest])
            .map(userService.createUser)
        }

      _ = otherUsers.length mustBe 10

      userById <- userService.getUserById(user.userId)

      existingUsername <- userService.usernameExists(createUserRequest.username)
      nonExistingUsername <- userService.usernameExists(s"${createUserRequest.username}.123")

      assertions = {
        userById mustBe user
        existingUsername mustBe true
        nonExistingUsername mustBe false
      }

      _ <- app.stop()
    }
    yield assertions
  }
}
