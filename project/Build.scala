import sbt.Keys._
import sbt._

object BuildSettings {
  val paradiseVersion = "2.0.0"
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.github.dcapwell",
    version := "0.1.0",
    scalacOptions ++= Seq(),
//    scalaVersion := "2.11.1",
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  )
}

object PlaygroundBuild extends Build {

  import BuildSettings._

  lazy val root: Project = Project(
    "root",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core
    )
  ) aggregate(macros, core)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies += "org.scalamacros" %% "quasiquotes" % paradiseVersion,

      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    )
  )

  lazy val core: Project = Project(
    "core",
    file("core"),
    settings = buildSettings
  ) dependsOn(macros)


//  val sharedSettings = Project.defaultSettings ++ Seq(
//    organization := "com.github.dcapwell",
//    scalaVersion := "2.10.4",
//    version := "0.1.0",
//    crossScalaVersions := Seq("2.10.4", "2.11.1"),
//    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
//    javacOptions in doc := Seq("-source", "1.7"),
//    parallelExecution in Test := true,
//    scalacOptions ++= Seq(Opts.compile.unchecked, Opts.compile.deprecation, Opts.compile.explaintypes),
//    resolvers += Resolver.sonatypeRepo("releases"),
//    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full)
//  )
//
//  lazy val macros = Project(
//    id = "macros",
//    base = file("macros"),
//    settings = sharedSettings ++ Seq(
//      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
//      libraryDependencies += "org.scalamacros" % "quasiquotes" % "2.0.0-M3" cross CrossVersion.full,
//
//      libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
//    )
//  )
}


// vim: set ts=4 sw=4 et:
