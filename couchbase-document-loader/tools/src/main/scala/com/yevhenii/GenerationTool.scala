package com.yevhenii

import com.yevhenii.generators._
import com.yevhenii.utils.{Configs, FileUtils}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object GenerationTool {

  val adjectives = FileUtils.readLines(Configs.ADJECTIVES_FILE)
  val animals = FileUtils.readLines(Configs.ANIMALS_FILE)

  val nameGenerator = NameGenerator(adjectives, animals)
  val ageGenerator = AgeGenerator()
  val randomGenerator = RandomGenerator()

  val gen = PetGenerator(adjectives, animals)


  def main(args: Array[String]): Unit = {
    val resultFile = getArg(args, 0).getOrElse(Configs.DEFAULT_RESULT_FILE)
    val objectCount = getArg(args, 1).fold(Configs.DEFAULT_OBJECT_COUNT)(_.toInt)
    val threadsCount = getArg(args, 2).fold(Configs.DEFAULT_THREADS)(_.toInt)

    val writer = FileUtils.objectWriter[Pet](resultFile)

    val onPartComplete = (list: List[Pet]) => list.map(writer(_))

    val time = System.currentTimeMillis()
    Await.ready(
      ParallelGenerator.generate(gen, objectCount)(threadsCount, onPartComplete),
      Duration.Inf
    )
    println(s"completed in: ${System.currentTimeMillis() - time} ms")
  }

  def getArg(args: Array[String], indx: Int): Option[String] = {
    if (args.length >= indx + 1)
      Some(args(indx))
    else
      None
  }
}
