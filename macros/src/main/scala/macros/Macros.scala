package macros

import language.experimental.macros

import scala.reflect.macros.{whitebox, blackbox}
import whitebox.Context // default to whitebox to be compatable

object Macros {

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

  def debug1(param: Any): Unit = macro DebugMacros.debug1

  def debug(params: Any*): Unit = macro DebugMacros.debug

  def sdebug(params: Any*): String = macro DebugMacros.sdebug

  def caseFields[A](): String = macro CaseClassMacros.caseFields[A]

  def createFromTrait(name: String): Unit = macro createFromTrait_impl

  def createFromTrait_impl(c: Context)(name: c.Expr[String]) = {
    import c.universe._

    val memberName = name.tree match {
      case Literal(Constant(lit: String)) => TypeName(lit)
      case _ => c.abort(c.enclosingPosition, "I need a literal!")
    }

    c.Expr(q"""new $memberName{} """)
  }

  def extractCaseName[T]: String = macro CaseClassMacros.name[T]

//  def extractCaseClassFields[T](t: T): Any = macro CaseClassMacros.fields[T]

  class CaseClassMacros(val c: whitebox.Context) extends CaseClassMacroBox {
    import c.universe._

    def name[T: c.WeakTypeTag]: c.Expr[String] = {
      val t = weakTypeOf[T]
      assertCaseClass(t)

     c.Expr[String](Literal(Constant(t.toString)))
    }

    def caseFields[A : c.WeakTypeTag](): c.Expr[String] = {
      val tag = implicitly[WeakTypeTag[A]]
      val data = caseMethods(tag.tpe) map(_.name) map(_.toString) mkString(", ")
      c.Expr[String](Literal(Constant(data)))
    }

  }

  trait CaseClassMacroBox { self =>

    val c: whitebox.Context

    import c.universe._

    def isCaseClass(t: Type): Boolean = {
      val sym = t.typeSymbol
      sym.isClass && sym.asClass.isCaseClass
    }

    def assertCaseClass(t: Type): Unit = {
      if(!isCaseClass(t)) c.abort(c.enclosingPosition, s"${t.typeSymbol} is not a case class")
    }

    def caseMethods(t: Type): List[MethodSymbol] = t.members.collect {
      case m: MethodSymbol if m.isCaseAccessor => m
    }.toList
  }

  class DebugMacros(val c: blackbox.Context) extends ExprMacroBox with LiteralMacroBox {
    import c.universe._

    def debug1(param: Expr[Any]): Expr[Unit] = {
      val paramRep = show(param.tree)
      val paramRepTree = Literal(Constant(paramRep))
      val paramRepExpr = c.Expr[String](paramRepTree)

      reify { println(paramRepExpr.splice + " = " + param.splice) }
//      q""" print($paramRepExpr + " = " + $param) """
      c.Expr[Unit](q""" println($paramRepExpr + " = " + $param) """)
    }

    def debug(params: Expr[Any]*): Expr[Unit] = {
      val trees = params.map { param =>
        if(isLiteral(param)) q"print($param)"
        else {
          val paramRep = show(param.tree)
          val paramRepTree = Literal(Constant(paramRep))
          val paramRepExpr = c.Expr[String](paramRepTree)
          q"""print($paramRepExpr + " = " + $param)"""
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
          q""" $builder.append($paramRepExpr).append(" = ").append($param) """
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

}
