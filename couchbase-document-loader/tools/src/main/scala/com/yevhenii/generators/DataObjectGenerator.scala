package com.yevhenii.generators

import com.yevhenii.SimpleDataObject


class DataObjectGenerator(adjectives: Vector[String], animals: Vector[String], var startId: Int)
  extends Generator[SimpleDataObject] {

  val nameGenerator = NameGenerator(adjectives, animals)
  val ageGenerator = AgeGenerator()
  val randomGenerator = RandomGenerator()

  override def generate(): SimpleDataObject = {
    //    increase id counter
    startId += 1

    val name = nameGenerator.generate()
    val age = ageGenerator.generate()
    val randomTuple = randomGenerator.generate()

    SimpleDataObject(startId - 1, name, age, randomTuple._1, randomTuple._2)
  }
}

object DataObjectGenerator {
  def apply(adjectives: Vector[String], animals: Vector[String]): Int => DataObjectGenerator =
    (startId: Int) =>
      new DataObjectGenerator(adjectives, animals, startId)
}