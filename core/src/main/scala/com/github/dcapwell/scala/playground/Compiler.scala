package com.github.dcapwell.scala.playground

import java.net.{URL, URLClassLoader}

import org.scalamacros.paradise.{Plugin => MacroParadisePlugin}

import scala.tools.reflect.ToolBox

// Look into https://github.com/twitter/util/blob/master/util-eval/src/main/scala/com/twitter/util/Eval.scala to replace this
class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {
  private[this] val commandPrefix = initialCommands mkString "\n"

  val toolBox: ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = options mkString ", ")
  }

  // this forces the compiler state to be loaded before any tests get run
  //TODO figure out if this is an issue with the add* methods
  eval("1 + 2")

  def parse(code: String) =
    toolBox.parse(s"$commandPrefix\n$code")

  def eval(code: String): Any =
    toolBox.eval(parse(code))

  def addOption(o: String): Compiler = new Compiler(o :: options, initialCommands)
  def addOptions(o: List[String]): Compiler = new Compiler(o ::: options, initialCommands)

  def addCommand(c: String): Compiler = new Compiler(options, c :: initialCommands)
  def addCommands(c: List[String]): Compiler = new Compiler(options, c ::: initialCommands)
  def addCommands(c: String*): Compiler = new Compiler(options, c.toList ::: initialCommands)
}

object Compiler {
  lazy val SysClasspath = System.getProperty("java.class.path").split(":").toList

  // thread class loader will be "sbt-launcher.jar" if in sbt
  // use the compiler's class loader to get the classes we care about
  lazy val Classpath: List[String] = classOf[Compiler].getClassLoader match {
    case u: URLClassLoader => u.getURLs.toList.map(_.toString)
    case _ => SysClasspath // this isn't always going to work.  If JVM doesn't use URLClassLoader and running inside sbt, this will be garbage
  }

  lazy val ClasspathOption: String = s"-cp ${Classpath.mkString(":")}"

  lazy val Plugins: List[String] = List(classOf[MacroParadisePlugin]).map(findPath).map(_.getPath)

  private def findPath(clazz: Class[_]): URL =
    clazz.getProtectionDomain.getCodeSource.getLocation

  lazy val PluginOptions: List[String] = Plugins.map(p => s"-Xplugin:$p")

  lazy val DefaultOptions: List[String] = ClasspathOption :: PluginOptions

  val DefaultCompiler = new Compiler(DefaultOptions)

  def initialCommands(cmds: List[String]): Compiler =
    new Compiler(initialCommands = cmds)

  def initialCommands(cmds: String*): Compiler =
    new Compiler(initialCommands = cmds.toList)
}
