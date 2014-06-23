package macros

import scala.tools.reflect.ToolBox

class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {

  val Classath: String = System.getProperty("java.class.path")

  val toolBox: ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = (s"-cp $Classath" :: options) mkString ", ")
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
