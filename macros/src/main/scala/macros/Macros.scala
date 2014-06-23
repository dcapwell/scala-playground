package macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import scala.reflect.macros.blackbox.Context // default to whitebox to be compatable

object Macros extends CaseFunctions with DebugFunctions {

  def hello(): Unit = macro hello_impl

  def hello_impl(c: Context)(): c.Expr[Unit] = {
    import c.universe._

    reify { println("Hello World") }
  }

  def printparam(param: Any): Unit = macro printparam_impl

  def printparam_impl(c: Context)(param: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe._

    reify { println(param.splice) }
  }

  def createFromTrait(name: String): Unit = macro createFromTrait_impl

  def createFromTrait_impl(c: Context)(name: c.Expr[String]) = {
    import c.universe._

    val memberName = name.tree match {
      case Literal(Constant(lit: String)) => TypeName(lit)
      case _ => c.abort(c.enclosingPosition, "I need a literal!")
    }

    c.Expr(q"""new $memberName{} """)
  }

}

