package web.controllers

import java.util.UUID

import com.ruchij.shared.exceptions.ValidationException
import com.ruchij.shared.models.User
import com.ruchij.shared.test.utils.Matchers.{beJson, equalJsonOf}
import com.ruchij.shared.test.utils.RandomGenerator
import com.ruchij.shared.test.utils.TestUtils._
import com.ruchij.shared.utils.SystemUtilities
import com.ruchij.shared.web.responses.models.ExceptionResponse
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import utils.GuiceUtils.application
import utils.Random.createUserRequestGenerator
import web.requests.models.CreateUserRequest

class UserControllerSpec extends PlaySpec {

  "UserController POST /user" should {
    "successfully create a user" in {

      val timestamp = SystemUtilities.currentTime()
      val uuid = SystemUtilities.randomUuid()

      val systemUtilities = new SystemUtilities {
        override def currentTime(): DateTime = timestamp
        override def randomUuid(): UUID = uuid
      }

      val createUserRequest = RandomGenerator.generate[CreateUserRequest]
      val request = postRequest("/user", createUserRequest)

      val app = application(classOf[SystemUtilities] -> systemUtilities)
      val response = route(app, request).value

      val expectedCreatedUser =
        User(
          uuid,
          timestamp,
          createUserRequest.username,
          createUserRequest.firstName,
          createUserRequest.lastName,
          createUserRequest.email,
          createUserRequest.profileImageId
        )

      status(response) mustBe CREATED
      contentType(response) must beJson
      contentAsJson(response) must equalJsonOf(expectedCreatedUser)

      await(app.stop())
    }

    "return a validation failure when the username field is empty" in {

      val createUserRequest = RandomGenerator.generate[CreateUserRequest]
      val request = postRequest("/user", createUserRequest.copy(username = StringUtils.EMPTY))

      val app = application()
      val response = route(app, request).value

      status(response) mustBe BAD_REQUEST
      contentType(response) must beJson
      contentAsJson(response) must equalJsonOf(ExceptionResponse(List(ValidationException("username must NOT be empty"))))

      await(app.stop())
    }

    "return a validation failure when password is less than 8 characters and the firstName field is empty" in {

      val createUserRequest = RandomGenerator.generate[CreateUserRequest]
      val request = postRequest("/user", createUserRequest.copy(firstName = StringUtils.EMPTY, password = "secret"))

      val app = application()
      val response = route(app, request).value

      status(response) mustBe BAD_REQUEST
      contentType(response) must beJson
      contentAsJson(response) must equalJsonOf {
        ExceptionResponse {
          List(
            ValidationException("firstName must NOT be empty"),
            ValidationException("password length must be greater than 8 characters")
          )
        }
      }

      await(app.stop())
    }
  }
}
