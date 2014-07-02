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
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
      "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
    ),
    addCompilerPlugin("org.scalamacros" %% "paradise" % paradiseVersion cross CrossVersion.full)
  )

  lazy val root: Project = Project(
    "root",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in core
    )
  ) aggregate(macros, core) dependsOn(macros, core)

  lazy val core: Project = Project(
    "core",
    file("core"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _),

      libraryDependencies += "com.chuusai" %% "shapeless" % "2.0.0",
      libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6",
      libraryDependencies += "org.spire-math" %% "spire" % "0.7.5",
      libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7",

      // https://github.com/typelevel/shapeless-contrib is required to build locally first
      // don't use these yet, so removing for now
      // libraryDependencies += "org.typelevel" %% "shapeless-spire" % "0.3-SNAPSHOT",
      // libraryDependencies += "org.typelevel" %% "shapeless-scalaz" % "0.3-SNAPSHOT",

      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
      // libraryDependencies += "org.typelevel" %% "shapeless-scalacheck" % "0.3-SNAPSHOT" % "test"
    )
  )

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-compiler" % _),
      //      libraryDependencies += "org.scalamacros" %% "quasiquotes" % paradiseVersion, // only needed in 2.10

      libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.0" % "test"
    )
  ) dependsOn(core % "test")

}


// vim: set ts=4 sw=4 et:
