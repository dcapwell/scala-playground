package com.github.dcapwell.scala.playground.macros

import scala.reflect.macros.whitebox

trait CaseFunctions {
  import scala.language.experimental.macros

  def caseFields[A](): List[String] = macro CaseMacros.caseFieldsMacro[A]

  def extractCaseName[A]: String = macro CaseMacros.nameMacro[A]

  def toMap[A](a: A): Map[String, Any] = macro CaseMacros.toMapMacro[A]

  def fromMap[A](m: Map[String, Any]): A = macro CaseMacros.fromMapMacro[A]

  def toTuple[A](a: A): Any = macro CaseMacros.toTupleMacro[A]

  def fromTuple[CaseClass](p: Product): CaseClass = macro CaseMacros.fromTupleMacro[CaseClass]

  def matchType[A]: Unit = macro CaseMacros.matchType[A]
}

class CaseMacros(val c: whitebox.Context) extends WhiteboxSupport {

  import c.universe._

  def matchType[A : c.WeakTypeTag]: c.Expr[Unit] = {
    val tpe = weakTypeOf[A]

    is(tpe, "scala.Tuple3") match {
      case Some(e) => c.abort(c.enclosingPosition, e)
      case None => c.Expr[Unit](Literal(Constant(())))
    }
  }

  def nameMacro[A: c.WeakTypeTag]: c.Expr[String] = {
    val a = weakTypeOf[A]
    assertCaseClass(a)

    val fields = caseFields(a) map(f => s"${f.name.toString}: ${f.typeSignature}") mkString(", ")

    c.Expr[String](Literal(Constant(s"$a($fields)")))
  }

  def caseFieldsMacro[A : c.WeakTypeTag](): c.Expr[List[String]] = {
    val tpe = weakTypeOf[A]

    val params = caseFields(tpe)

    val data = params.map(_.name).map(_.toString)

    c.Expr[List[String]](q"$data")
  }

  def toMapMacro[A : c.WeakTypeTag](a: c.Expr[A]): c.Expr[Map[String, Any]] = {
    val tpe = weakTypeOf[A]

    val params = caseFields(tpe)

    val mapEntries = params map { param =>
      val name = param.name
      val mapKey: String = name.decodedName.toString

      q"($mapKey -> ${a.tree}.${name.toTermName})"
    }

    c.Expr[Map[String, Any]](q"Map(..$mapEntries)")
  }

  def fromMapMacro[A: c.WeakTypeTag](m: c.Expr[Map[String, Any]]): c.Expr[A] = {
    val tpe = weakTypeOf[A]

    val params = caseFields(tpe)
    val companion = companionObject(tpe)

    val mapEntries = params map { param =>
      val name = param.name
      val mapKey: String = name.decodedName.toString
      val returnType = tpe.decl(name).typeSignature

      q"${m.tree}($mapKey).asInstanceOf[$returnType]"
    }

    c.Expr[A](q"$companion(..$mapEntries)")
  }

  def toTupleMacro[A : c.WeakTypeTag](a: c.Expr[A]): Tree = {
    val tpe = weakTypeOf[A]

    val params = caseFields(tpe)

    val paramCalls = params map { param =>
      val name = param.name

      q"${a.tree}.${name.toTermName}"
    }

    q"(..$paramCalls)"
  }

  def fromTupleMacro[CaseClass : c.WeakTypeTag](p: c.Expr[Product]): Tree = {
    val caseClassType = weakTypeOf[CaseClass]

    // get case class params list
    val params = caseFields(caseClassType).map(_.typeSignature.typeSymbol)

    val givenParams = typeSymbols(p.actualType)

    val size = params.size
    val expectedType = s"scala.Tuple$size"
    is(p.actualType, expectedType) match {
      case Some(e) => c.abort(c.enclosingPosition, e)
      case None =>
        if(givenParams == params) {
          // convert
          val comp = companionObject(caseClassType)
          //TODO check for <none> which seems to happen when you define the case class in the compiler eval statement

          val tuped = (1 to size) map{index => Select(p.tree, TermName(s"_$index"))}

          q"$comp(..$tuped)"
        } else c.abort(c.enclosingPosition, s"Case class params ($params) does not match given params ($givenParams)")
    }
  }

}

object Case extends CaseFunctions
