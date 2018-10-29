package com.yevhenii.utils

import java.io.FileWriter

import com.google.gson.Gson

import scala.io.Source

object FileUtils {

  def readLines(file: String): Vector[String] = {
    Source.fromResource(file).getLines()
      .map(_.trim)
      .filterNot(_.isEmpty)
      .map(_.toLowerCase.capitalize)
      .toVector
  }

  def objectWriter[A](file: String): A => A = {
    val writer = new FileWriter(file)
    val gson = new Gson

    obj: A => {
      writer.append(s"${gson.toJson(obj)}\r\n").flush()
      obj
    }
  }
}
