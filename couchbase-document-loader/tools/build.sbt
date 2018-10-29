name := "generation-tool"

version := "0.1"

scalaVersion := "2.12.6"


libraryDependencies += "com.google.code.gson" % "gson" % "2.8.5"
libraryDependencies += "com.typesafe" % "config" % "1.3.2"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.11-SNAPSHOT" % "test"

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "com.yevhenii",
  scalaVersion := "2.12.6",
  test in assembly := {}
)