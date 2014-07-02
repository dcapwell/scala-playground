package compiler

import java.net.URLClassLoader

import scala.tools.reflect.ToolBox

// Look into https://github.com/twitter/util/blob/master/util-eval/src/main/scala/com/twitter/util/Eval.scala to replace this
class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {

  lazy val SysClasspath = System.getProperty("java.class.path").split(":").toList

  val Classath: List[String] = Thread.currentThread().getContextClassLoader match {
    case u: URLClassLoader => u.getURLs.toList.map(_.toString)
    case _ => SysClasspath
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
