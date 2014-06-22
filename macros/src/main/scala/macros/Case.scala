package macros

import language.experimental.macros

import scala.reflect.macros.whitebox

trait CaseFunctions {
  def caseFields[A](): String = macro CaseClassMacros.caseFields[A]

  def extractCaseName[T]: String = macro CaseClassMacros.name[T]
}

object Case extends CaseFunctions

class CaseClassMacros(val c: whitebox.Context) extends CaseClassMacroBox {
  import c.universe._

  def name[A: c.WeakTypeTag]: c.Expr[String] = {
    val a = weakTypeOf[A]
    assertCaseClass(a)

    c.Expr[String](Literal(Constant(a.toString)))
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