package com.ruchij.shared.test.utils

import scala.util.Random

trait RandomGenerator[+A] {
  self =>
  def map[B](f: A => B): RandomGenerator[B] = () => f(self.generate())

  def flatMap[B](f: A => RandomGenerator[B]): RandomGenerator[B] = () => f(self.generate()).generate()

  def generate(): A
}

object RandomGenerator {
  def generate[A](implicit randomGenerator: RandomGenerator[A]): A = randomGenerator.generate()

  def generator[A](value: => A): RandomGenerator[A] = () => value

  implicit val booleanGenerator: RandomGenerator[Boolean] = () => Random.nextBoolean()

  implicit def optionGenerator[A](implicit randomGenerator: RandomGenerator[A]): RandomGenerator[Option[A]] =
    booleanGenerator.flatMap { boolean => if (boolean) randomGenerator.map(Some.apply) else generator(None) }
}
