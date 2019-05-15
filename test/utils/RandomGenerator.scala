package utils

import com.github.javafaker.Faker
import web.requests.models.CreateUserRequest

import scala.util.Random

trait RandomGenerator[+A] {
  self =>
  def map[B](f: A => B): RandomGenerator[B] = () => f(self.generate())

  def flatMap[B](f: A => RandomGenerator[B]): RandomGenerator[B] = () => f(self.generate()).generate()

  def generate(): A
}

object RandomGenerator {
  val FAKER: Faker = Faker.instance()
  import FAKER._

  def generate[A](implicit randomGenerator: RandomGenerator[A]): A = randomGenerator.generate()

  def generator[A](value: => A): RandomGenerator[A] = () => value

  implicit val booleanGenerator: RandomGenerator[Boolean] = () => Random.nextBoolean()

  implicit def optionGenerator[A](implicit randomGenerator: RandomGenerator[A]): RandomGenerator[Option[A]] =
    booleanGenerator.flatMap { boolean => if (boolean) randomGenerator.map(Some.apply) else generator(None) }

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
