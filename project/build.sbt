organization := "scalation"

name := "scalation-analytics"

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.1"

scalacOptions += "-Xexperimental"

javaOptions += "-Xprof"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.12" % "3.0.0" % "test",
    "net.sourceforge.owlapi" % "owlapi-distribution" % "3.5.1",
    "net.sourceforge.owlapi" % "owlexplanation" % "1.1.0",
    "com.hermit-reasoner" % "org.semanticweb.hermit" % "1.3.8.4",
    "net.sourceforge.owlapi" % "jfact" % "1.2.2",
    "org.jsoup" % "jsoup" % "1.8.2",
    "com.typesafe.play" % "play-json_2.11" % "2.4.3",
    "org.apache.commons" % "commons-math3" % "3.6.1"
//    "org.apache.spark" % "spark-core_2.11" % "1.6.1",
//    "org.apache.spark" % "spark-mllib_2.11" % "1.6.1"
)


