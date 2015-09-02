
organization := "scalation"

name := "scalation-analytics"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions += "-Xexperimental"

fork in run := true

libraryDependencies ++= Seq(
    "scalation" % "scalation_2.11" % "1.2",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "net.sourceforge.owlapi" % "owlapi-distribution" % "3.5.1",
    "net.sourceforge.owlapi" % "owlexplanation" % "1.1.0",
    "com.hermit-reasoner" % "org.semanticweb.hermit" % "1.3.8.4",
    "net.sourceforge.owlapi" % "jfact" % "1.2.2",
    "org.jsoup" % "jsoup" % "1.8.1")


