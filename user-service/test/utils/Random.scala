package utils

import com.github.javafaker.Faker
import com.ruchij.shared.test.utils.RandomGenerator
import com.ruchij.shared.test.utils.RandomGenerator._
import web.requests.models.CreateUserRequest

object Random {
  val faker: Faker = Faker.instance()

  import faker._

  implicit val createUserRequestGenerator: RandomGenerator[CreateUserRequest] =
    for {
      username <- generator(name().username())
      firstName <- generator(name().firstName())
      lastName <- optionGenerator(generator(name().lastName()))
      profileImage <- optionGenerator(generator(internet().url()))
      password <- generator(internet().password(10, 20))
      email <- generator(internet().emailAddress())
    }
      yield CreateUserRequest(username, firstName, lastName, profileImage, password, email)
}
