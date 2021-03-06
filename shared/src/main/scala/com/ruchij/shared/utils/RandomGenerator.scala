package com.ruchij.shared.utils

import java.util.concurrent.TimeUnit

import com.github.javafaker.Faker
import com.ruchij.shared.models.User

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

trait RandomGenerator[+A] {
  self =>
  def map[B](f: A => B): RandomGenerator[B] = () => f(self.generate())

  def flatMap[B](f: A => RandomGenerator[B]): RandomGenerator[B] = () => f(self.generate()).generate()

  def generate(): A
}

object RandomGenerator {
  val faker: Faker = Faker.instance()

  import faker._

  def generate[A](implicit randomGenerator: RandomGenerator[A]): A = randomGenerator.generate()

  def generator[A](value: => A): RandomGenerator[A] = () => value

  implicit val booleanGenerator: RandomGenerator[Boolean] = () => Random.nextBoolean()

  def finiteDurationGenerator(maxMillis: Int): RandomGenerator[FiniteDuration] =
    () => FiniteDuration(Random.nextInt(maxMillis), TimeUnit.MILLISECONDS)

  def intGenerator(maximum: Int): RandomGenerator[Int] = () => math.abs(Random.nextInt(maximum))

  implicit def optionGenerator[A](implicit randomGenerator: RandomGenerator[A]): RandomGenerator[Option[A]] =
    booleanGenerator.flatMap { boolean => if (boolean) randomGenerator.map(Some.apply) else generator(None) }

  implicit def userGenerator(implicit systemUtilities: SystemUtilities): RandomGenerator[User] =
    for {
      userId <- generator(systemUtilities.randomUuid())
      username <- generator(name().username())
      firstName <- generator(name().firstName())
      lastName <- optionGenerator(generator(name().lastName()))
      email <- generator(internet().emailAddress())
      profileImageId <- optionGenerator(generator(internet().slug()))
    }
    yield User(userId, systemUtilities.currentTime(), username, firstName, lastName, email, profileImageId)
}
