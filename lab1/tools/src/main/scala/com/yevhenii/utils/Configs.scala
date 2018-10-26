package com.yevhenii.utils

import com.typesafe.config.ConfigFactory

object Configs {
  private val config = ConfigFactory.load()

  val DEFAULT_OBJECT_COUNT = config.getInt("object-count")
  val DEFAULT_THREADS = config.getInt("threads")
  val DEFAULT_RESULT_FILE = config.getString("files.output")

  val ADJECTIVES_FILE = config.getString("files.adjectives")
  val ANIMALS_FILE = config.getString("files.animals")

}
