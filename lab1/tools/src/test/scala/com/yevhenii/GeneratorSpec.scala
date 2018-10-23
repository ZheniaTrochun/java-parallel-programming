package com.yevhenii

import com.yevhenii.generators.{Generator, ParallelGenerator}
import org.scalameter.{Bench, Key, Warmer, config}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class GeneratorSpec extends FlatSpec with Matchers {

  val OBJECT_COUNT = 10000
  val random = new scala.util.Random()
  val generator = (_: Int) => DefinedObjectGenerator

  "ParallelGenerator" should "generate correct number of objects while run sequentially" in {
    val future = ParallelGenerator.generate(generator, OBJECT_COUNT)(1, x => x)

    future.foreach(list =>
      list.size should equal (OBJECT_COUNT)
    )
  }

  "ParallelGenerator" should "generate correct number of objects while run concurrently" in {
    val future = ParallelGenerator.generate(generator, OBJECT_COUNT)(10, x => x)

    future.foreach(list =>
      list.size should equal (OBJECT_COUNT)
    )
  }

//  "ParallelGenerator" should "sequential generation should be slower than concurrent generation" in {
//    val standardConfig = config(
//      Key.exec.minWarmupRuns -> 20,
//      Key.exec.maxWarmupRuns -> 50,
//      Key.exec.benchRuns -> 25,
//      Key.verbose -> true
//    ) withWarmer(new Warmer.Default)
//
//
//    val parTime = standardConfig measure {
//      Await.result(ParallelGenerator.generate(generator, OBJECT_COUNT)(10, x => x), 10 seconds)
//    }
//
//    val seqTime = standardConfig measure {
//      Await.result(ParallelGenerator.generate(generator, OBJECT_COUNT)(1, x => x), 10 seconds)
//    }
//
//    val minSpeedup = Runtime.getRuntime.availableProcessors / 3
//    val speedup = seqTime.value / parTime.value
//
//    println(s"sequential time: $seqTime")
//    println(s"parallel time: $parTime")
//    println(s"speedup: $speedup")
//
//    assert(seqTime.value > parTime.value)
//    assert(speedup >= minSpeedup)
//  }

  case class DefinedObject(str: String, num: Int)

  val DefinedObjectGenerator = Generator(() => DefinedObject(random.alphanumeric.take(5).mkString, random.nextInt()))
}
