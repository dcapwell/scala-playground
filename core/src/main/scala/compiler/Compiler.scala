package compiler

import java.net.{URL, URLClassLoader}

import scala.tools.reflect.ToolBox

import org.scalamacros.paradise.{Plugin => MacroParadisePlugin}

// Look into https://github.com/twitter/util/blob/master/util-eval/src/main/scala/com/twitter/util/Eval.scala to replace this
class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {

  lazy val SysClasspath = System.getProperty("java.class.path").split(":").toList

  // thread class loader will be "sbt-launcher.jar" if in sbt
  // use the compiler's class loader to get the classes we care about
  val Classpath: List[String] = classOf[Compiler].getClassLoader match {
    case u: URLClassLoader => u.getURLs.toList.map(_.toString)
    case _ => SysClasspath // this isn't always going to work.  If JVM doesn't use URLClassLoader and running inside sbt, this will be garbage
  }

  val Plugins: List[String] = List(classOf[MacroParadisePlugin]).map(findPath).map(_.getPath)

  val toolBox: ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = (s"-cp ${Classpath.mkString(":")}" :: options ::: Plugins.map(p => s"-Xplugin:$p")) mkString ", ")
  }

  private def findPath(clazz: Class[_]): URL =
    clazz.getProtectionDomain.getCodeSource.getLocation

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
