import sbt.Keys._
import sbt._

object build extends Build {

  val paradiseVersion = "2.0.0"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.github.dcapwell",
    version := "0.1.0",
    scalacOptions ++= Seq("-deprecation", "-feature"),
    scalaVersion := "2.11.1",
    crossScalaVersions := Seq("2.11.0", "2.11.1"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" %% "paradise" % paradiseVersion cross CrossVersion.full)
  )

  lazy val root: Project = Project(
    "root",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core
    )
  ) aggregate(macros, core) dependsOn(macros, core)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
//      libraryDependencies += "org.scalamacros" %% "quasiquotes" % paradiseVersion, // only needed in 2.10

      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    )
  )

  lazy val core: Project = Project(
    "core",
    file("core"),
    settings = buildSettings
  ) dependsOn(macros)

}


// vim: set ts=4 sw=4 et:
