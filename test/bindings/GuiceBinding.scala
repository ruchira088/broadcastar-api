package bindings
import play.api.inject.{Binding, bind}

import scala.language.implicitConversions

sealed trait GuiceBinding[A] {
  def bindClazz(): Binding[A]
}

object GuiceBinding {
  case class GuiceClassBinding[A, B <: A](clazz: Class[A], bindingClazz: Class[B]) extends GuiceBinding[A] {
    override def bindClazz(): Binding[A] = bind(clazz).to(bindingClazz)
  }

  case class GuiceInstanceBinding[A, B <: A](clazz: Class[A], bindingInstance: B) extends GuiceBinding[A] {
    override def bindClazz(): Binding[A] = bind(clazz).to(bindingInstance)
  }

  implicit def guiceClassBinding[A, B <: A](binding: (Class[A], Class[B])): GuiceClassBinding[A, B] = {
    val (clazz, bindingClazz) = binding
    GuiceClassBinding(clazz, bindingClazz)
  }

  implicit def guiceInstanceBinding[A, B <: A](binding: (Class[A], B)): GuiceInstanceBinding[A, B] = {
    val (clazz, instanceBinding) = binding
    GuiceInstanceBinding(clazz, instanceBinding)
  }
}