package compiler

import java.io.File

import scala.tools.reflect.ToolBox

class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {

  val SysClasspath = System.getProperty("java.class.path").split(":").toList

  val Classath: List[String] = SysClasspath match {
    // when run in SBT the call to System.getProperties will only contain the sbt jar
    case x :: Nil if x.endsWith("sbt-launch.jar") =>
      (for {
        children <- new File(".").getAbsoluteFile.listFiles()
        compiledDir <- List(new File(children, "target/scala-2.11/classes"), new File(children, "target/scala-2.11/test-classes"))
        if compiledDir.exists()
      } yield compiledDir.toString).toList
    case xs => xs
  }

  val toolBox: ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = (s"-cp ${Classath.mkString(":")}" :: options) mkString ", ")
  }

  def parse(code: String) =
    toolBox.parse(s"${initialCommands.mkString("\n")}\n$code")

  def eval(code: String): Any =
    toolBox.eval(parse(code))

}

object Compiler {
  val DefaultCompiler = new Compiler(List())

  def initialCommands(cmds: List[String]): Compiler =
    new Compiler(initialCommands = cmds)

  def initialCommands(cmds: String*): Compiler =
    new Compiler(initialCommands = cmds.toList)
}
