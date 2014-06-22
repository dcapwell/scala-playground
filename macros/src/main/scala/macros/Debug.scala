package macros

import language.experimental.macros

import scala.reflect.macros.blackbox

trait DebugFunctions {
  def debug1(param: Any): Unit = macro DebugMacros.debug1

  def debug(params: Any*): Unit = macro DebugMacros.debug

  def sdebug(params: Any*): String = macro DebugMacros.sdebug
}

object Debug extends DebugFunctions

class DebugMacros(val c: blackbox.Context) extends ExprMacroBox with LiteralMacroBox {
  import c.universe._

  def debug1(param: Expr[Any]): Expr[Unit] = {
    val paramRep = show(param.tree)
    val paramRepTree = Literal(Constant(paramRep))
    val paramRepExpr = c.Expr[String](paramRepTree)

    c.Expr[Unit](q""" println($paramRepExpr + " = " + $param) """)
  }

  def debug(params: Expr[Any]*): Expr[Unit] = {
    val trees = params.map { param =>
      if(isLiteral(param)) q"print($param)"
      else {
        val paramRep = show(param.tree)
        val paramRepTree = Literal(Constant(paramRep))
        val paramRepExpr = c.Expr[String](paramRepTree)

        val tpe = param.actualType.toString

        q"""print($paramRepExpr + ": [" + $tpe + "]" + " = " + $param)"""
      }
    }

    // Inserting ", " between trees, and a println at the end.
    val separators = (1 to trees.size-1).map(_ => (reify { print(", ") }).tree) :+ (reify { println() }).tree
    val treesWithSeparators = trees.zip(separators).flatMap(p => List(p._1, p._2))

    c.Expr[Unit](Block(treesWithSeparators.toList, UnitLiteral))
  }

  def sdebug(params: Expr[Any]*): Expr[String] = {
    val builder = TermName("sb")
    val start = q""" val $builder = new java.lang.StringBuilder() """

    val trees = params.map { param =>
      if(isLiteral(param)) q""" $builder.append($param) """
      else {
        val paramRep = show(param.tree)
        val paramRepTree = Literal(Constant(paramRep))
        val paramRepExpr = c.Expr[String](paramRepTree)
        q""" $builder.append($paramRepExpr).append(": [").append(${param.actualType.toString}).append("] = ").append($param) """
      }
    }

    val sep = (1 to trees.size - 1).map(_ => q""" $builder.append(", ") """) :+ UnitLiteral
    val work = trees.zip(sep).flatMap(x => List(x._1, x._2))

    val end = q""" $builder.toString """
    c.Expr[String](Block(start :: work.toList, end))
  }
}

trait ExprMacroBox { self =>
  val c: blackbox.Context

  import c.universe._

  def isLiteral[A](a: Expr[A]): Boolean = a.tree match {
    case Literal(Constant(_)) => true
    case _ => false
  }
}

trait LiteralMacroBox { self =>
  val c: blackbox.Context

  import c.universe._

  val UnitLiteral = Literal(Constant(()))
}
