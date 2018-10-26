package com.yevhenii.generators


trait Generator[A] {
  val random = new scala.util.Random()

  def map[B](f: A => B): B =
    f( generate() )

  def flatMap[B](f: A => B): B =
    f( generate() )

  def generate(): A
}

object Generator {
  def apply[A](gen: => A): Generator[A] = new Generator[A] {
    override def generate(): A = gen
  }
}