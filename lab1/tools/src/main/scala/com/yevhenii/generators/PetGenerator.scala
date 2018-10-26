package com.yevhenii.generators

import com.yevhenii.Pet

class PetGenerator(adjectives: Vector[String], animals: Vector[String], var startId: Int)
  extends Generator[Pet] {

  val nameGenerator = NameGenerator(adjectives, animals)
  val ageGenerator = AgeGenerator()
  val randomGenerator = RandomGenerator()

  override def generate(): Pet = {
    //    increase id counter
    startId += 1

    val name = nameGenerator.generate()
    val age = ageGenerator.generate()
    val randomTuple = randomGenerator.generate()

    Pet(startId - 1, name, age, randomTuple._1, randomTuple._2)
  }
}

object PetGenerator {
  def apply(adjectives: Vector[String], animals: Vector[String]): Int => PetGenerator =
    (startId: Int) =>
      new PetGenerator(adjectives, animals, startId)
}