package com.yevhenii

import com.yevhenii.generators._
import com.yevhenii.utils.FileUtils

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GenerationTool {

  val DEFAULT_OBJECT_COUNT = 100000
  val DEFAULT_THREADS = 10
  val DEFAULT_RESULT_FILE = "data.txt"

  val ADJECTIVES_FILE = "adj.txt"
  val ANIMALS_FILE = "animals.txt"

  val adjectives = FileUtils.readLines(ADJECTIVES_FILE)
  val animals = FileUtils.readLines(ANIMALS_FILE)

  val nameGenerator = NameGenerator(adjectives, animals)
  val ageGenerator = AgeGenerator()
  val randomGenerator = RandomGenerator()

  val gen = DataObjectGenerator(adjectives, animals)


  def main(args: Array[String]): Unit = {
    val resultFile = if (args.nonEmpty) args(0) else DEFAULT_RESULT_FILE
    val objectCount = if (args.length >= 2) args(1).toInt else DEFAULT_OBJECT_COUNT
    val threadsCount = if (args.length >= 3) args(2).toInt else DEFAULT_THREADS

    val writer = FileUtils.objectWriter[SimpleDataObject](resultFile)

    val onPartComplete = (list: List[SimpleDataObject]) => list.map(writer(_))

    val time = System.currentTimeMillis()
    Await.ready(
      ParallelGenerator.generate(gen, objectCount)(threadsCount, onPartComplete),
      Duration.Inf
    )
    println(s"completed in: ${System.currentTimeMillis() - time} ms")
  }
}
