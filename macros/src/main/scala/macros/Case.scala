package macros

import language.experimental.macros

import scala.reflect.macros.whitebox

trait CaseFunctions {
  def caseFields[A](): String = macro CaseClassMacros.caseFields[A]

  def extractCaseName[A]: String = macro CaseClassMacros.name[A]

  def toMap[A](a: A): Map[String, Any] = macro CaseClassMacros.toMap[A]

  def fromMap[A](m: Map[String, Any]): A = macro CaseClassMacros.fromMap[A]

  def toTuple[A, B](a: A): B = macro CaseClassMacros.toTuple[A]
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

  def toMap[A : c.WeakTypeTag](a: c.Expr[A]): c.Expr[Map[String, Any]] = {
    val tpe = weakTypeOf[A]

    val declarations = tpe.decls
    val ctor = declarations.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get
    val params = ctor.paramLists.head

    val mapEntries = params map { param =>
      val name = param.name
      val mapKey: String = name.decodedName.toString

      q"($mapKey -> ${a.tree}.${name.toTermName})"
    }

    c.Expr[Map[String, Any]](q"Map(..$mapEntries)")
  }

  def fromMap[A: c.WeakTypeTag](m: c.Expr[Map[String, Any]]): c.Expr[A] = {
    val tpe = weakTypeOf[A]

    val declarations = tpe.decls
    val ctor = declarations.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get
    val params = ctor.paramLists.head

    val companion = tpe.typeSymbol.companionSymbol

    val mapEntries = params map { param =>
      val name = param.name
      val mapKey: String = name.decodedName.toString
      val returnType = tpe.declaration(name).typeSignature

      q"${m.tree}($mapKey).asInstanceOf[$returnType]"
    }

    c.Expr[A](q"$companion(..$mapEntries)")
  }

  def toTuple[A : c.WeakTypeTag](a: c.Expr[A]): Tree = {
    val tpe = weakTypeOf[A]

    val declarations = tpe.decls
    val ctor = declarations.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get
    val params = ctor.paramLists.head

    val paramCalls = params map { param =>
      val name = param.name

      q"${a.tree}.${name.toTermName}"
    }

    q"(..$paramCalls)"
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