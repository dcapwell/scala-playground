package compiler

import java.net.URLClassLoader

import scala.tools.nsc.Global
import scala.tools.reflect.{ToolBoxFactory, ToolBox}

// Look into https://github.com/twitter/util/blob/master/util-eval/src/main/scala/com/twitter/util/Eval.scala to replace this
class Compiler(options: List[String] = List(), initialCommands: List[String] = List()) {

  lazy val SysClasspath = System.getProperty("java.class.path").split(":").toList

  // thread class loader will be "sbt-launcher.jar" if in sbt
  // use the compiler's class loader to get the classes we care about
  val Classpath: List[String] = classOf[Compiler].getClassLoader match {
    case u: URLClassLoader => u.getURLs.toList.map(_.toString)
    case _ => SysClasspath
  }

  val toolBox: ToolBox[_ <: scala.reflect.api.Universe] = {
    val m = scala.reflect.runtime.currentMirror
    m.mkToolBox(options = (s"-cp ${Classpath.mkString(":")}" :: options) mkString ", ")
  }

  // inject macro paradise
  {
    eval("1 + 2") // needed to force the compiler to be generated
    val clazz = Class.forName("scala.tools.reflect.ToolBoxFactory$ToolBoxImpl")
    import scala.collection.convert.WrapAsScala._

    val withCompilerRef = clazz.getMethod("withCompilerApi")
    val withCompilerApi = withCompilerRef.invoke(toolBox)

    val moduleField = withCompilerApi.getClass.getDeclaredField("api$module")
    moduleField.setAccessible(true)
    val module = moduleField.get(withCompilerApi)

    val compilerRef = module.getClass.getDeclaredField("compiler")
    compilerRef.setAccessible(true)
    val global = compilerRef.get(module).asInstanceOf[Global]

    import org.scalamacros.paradise.{Plugin => MacroParadisePlugin}
    new MacroParadisePlugin(global)
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
