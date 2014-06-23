package macros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

trait CaseFunctions {
  def caseFields[A](): List[String] = macro CaseClassMacros.caseFieldsMacro[A]

  def extractCaseName[A]: String = macro CaseClassMacros.nameMacro[A]

  def toMap[A](a: A): Map[String, Any] = macro CaseClassMacros.toMapMacro[A]

  def fromMap[A](m: Map[String, Any]): A = macro CaseClassMacros.fromMapMacro[A]

  def toTuple[A](a: A): Any = macro CaseClassMacros.toTupleMacro[A]

  def fromTuple[A](p: Product): A = macro CaseClassMacros.fromTupleMacro[A]
}

object Case extends CaseFunctions

class CaseClassMacros(val c: whitebox.Context) extends CaseClassMacroBox {
  import c.universe._

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

  def fromTupleMacro[A : c.WeakTypeTag](p: c.Expr[Product]): Tree = {
    val tpe = weakTypeOf[A]

    // get case class params list
    val params = caseFields(tpe)

    // verify that tuple size matches params
    val size = params.size

    // get the types from each, and convert aliases into expected form
    val paramSigs = params map(_.typeSignature.dealias)
    val inputTypeArgs = p.actualType.typeArgs.map(_.dealias)

    //TODO find a way to validate that the type is TupleX vs anything that has matching type params
    if(inputTypeArgs == paramSigs) {
      // convert
      val comp = companionObject(tpe)

      val tuped = (1 to size) map{index => Select(p.tree, TermName(s"_$index"))}

      q"$comp(..$tuped)"
    } else c.abort(c.enclosingPosition, s"Expected type is scala.Tuple$size[${paramSigs.mkString(", ")}], but given ${p.actualType}")
  }

}

trait CaseClassMacroBox { self =>

  val c: whitebox.Context

  import c.universe._

  def isCaseClass(t: Type): Boolean = {
    val sym = t.typeSymbol
    sym.isClass && sym.asClass.isCaseClass
  }

  def assertCaseClass(t: Type): Unit =
    if(!isCaseClass(t)) c.abort(c.enclosingPosition, s"${t.typeSymbol} is not a case class")

  def primaryConstructor(t: Type): MethodSymbol =
    t.decls.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.get

  def companionObject(t: Type): Symbol =
    t.typeSymbol.companion

  def caseFields(t: Type): List[Symbol] =
    primaryConstructor(t).paramLists.head
}