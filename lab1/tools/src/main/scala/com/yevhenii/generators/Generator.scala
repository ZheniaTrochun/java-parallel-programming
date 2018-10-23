package com.yevhenii.generators

import com.yevhenii.GenerationTool.{adjectives, animals}
import com.yevhenii.SimpleDataObject

trait Generator[A] {
  val random = new scala.util.Random()

  def map[B](f: A => B): B =
    f( generate() )

  def flatMap[B](f: A => B): B =
    f( generate() )

  def generate(): A
}


case class NameGenerator(first: Vector[String], second: Vector[String]) extends Generator[String] {
  override def generate(): String = {
    s"${first(random.nextInt(first.length))} ${second(random.nextInt(second.length))}"
  }
}

case class AgeGenerator() extends Generator[Int] {
  override def generate(): Int = {
    random.nextInt(150)
  }
}

case class RandomGenerator() extends Generator[(Int, String)] {
  override def generate(): (Int, String) = {
    val number = random.nextInt()
    val string = random.alphanumeric.take(10).mkString
    number -> string
  }
}

class DataObjectGenerator(first: Vector[String], second: Vector[String], var startId: Int)
  extends Generator[SimpleDataObject] {

  val nameGenerator = NameGenerator(adjectives, animals)
  val ageGenerator = AgeGenerator()
  val randomGenerator = RandomGenerator()

  override def generate(): SimpleDataObject = {
//    increase id counter
    startId += 1

    nameGenerator.flatMap(str =>
      ageGenerator.flatMap(age =>
        randomGenerator.map(tuple =>
          SimpleDataObject(startId - 1, str, age, tuple._1, tuple._2))))
  }
}

object DataObjectGenerator {
  def apply(first: Vector[String], second: Vector[String]) =
    (startId: Int) =>
      new DataObjectGenerator(first, second, startId)
}

object Generator {
  def apply[A](gen: => A): Generator[A] = new Generator[A] {
    override def generate(): A = gen
  }
}