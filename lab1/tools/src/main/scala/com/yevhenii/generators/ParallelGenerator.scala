package com.yevhenii.generators

import scala.concurrent.{ExecutionContext, Future}

object ParallelGenerator {

  private def loop[A](list: List[A], count: Int, gen: Generator[A]): List[A] = {
    if (count > 0) loop (
        gen.generate() :: list,
        count - 1,
        gen
    ) else {
      list
    }
  }

//  futures implementation
  def generate[A](generator: Int => Generator[A], count: Int)(threads: Int, callback: List[A] => List[A])(implicit ex: ExecutionContext): Future[List[A]] = {
    Future.traverse ((0 until threads).toList) (i =>
      Future {
        callback(
          loop[A](Nil, count / threads, generator(i * count / threads))
        )
      }
    ).map(_.flatten)
  }

// scala parallel collection implementation

//  def generate[A](generator: Int => Generator[A], count: Int)(threads: Int, callback: List[A] => List[A])(implicit ex: ExecutionContext): List[A] = {
//    (0 until threads).par.map(i =>
//      callback(
//        loop[A](Nil, count/threads, generator(i * count/threads))
//      )
//    ).toList.flatten
//  }
}
