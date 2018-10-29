package com.yevhenii.generators


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
