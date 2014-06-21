package macros

import language.experimental.macros

import scala.reflect.macros.{whitebox, blackbox}
import whitebox.Context // default to whitebox to be compatable

object DebugMacros {

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

  def debug1(param: Any): Unit = macro debug1_impl

  def debug1_impl(c: Context)(param: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe._

    val paramRep = show(param.tree)
    val paramRepTree = Literal(Constant(paramRep))
    val paramRepExpr = c.Expr[String](paramRepTree)

    reify { println(paramRepExpr.splice + " = " + param.splice) }
  }

  def debug(params: Any*): Unit = macro debug_impl

  def debug_impl(c: Context)(params: c.Expr[Any]*): c.Expr[Unit] = {
    import c.universe._

    val trees = params.map { param =>
      param.tree match {
        case Literal(Constant(const)) =>
          val reified = reify { print(param.splice) }
          reified.tree
        case _ => {
          val paramRep = show(param.tree)
          val paramRepTree = Literal(Constant(paramRep))
          val paramRepExpr = c.Expr[String](paramRepTree)

          reify { print(paramRepExpr.splice + " = " + param.splice) }.tree
        }
      }
    }

    val separators = (1 to trees.size - 1).map(_ => reify { print(", ")}.tree ) :+ ( reify { println() }).tree
    val treesWithSeparators = trees.zip(separators).flatMap(p => List(p._1, p._2))

    c.Expr[Unit](Block(treesWithSeparators.toList, Literal(Constant(()))))
  }

  def extractName[A](): String = macro extractName_impl[A]

  def extractName_impl[A : c.WeakTypeTag](c: Context)(): c.Expr[String] = {
    import c.universe._

    val tag = implicitly[WeakTypeTag[A]]
    val data = tag.tpe.members.collect {
      case m: c.universe.MethodSymbol if m.isCaseAccessor => m
    } map(_.name) map(_.toString) mkString(", ")
    c.Expr[String](Literal(Constant(data)))
  }

  def createFromTrait(name: String): Unit = macro createFromTrait_impl

  def createFromTrait_impl(c: Context)(name: c.Expr[String]) = {
    import c.universe._

    val memberName = name.tree match {
      case Literal(Constant(lit: String)) => newTypeName(lit)
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

//    def fields[T : c.WeakTypeTag](t: T): Tree = {
//      val t = weakTypeOf[T]
//      assertCaseClass(t)
//
//      val fields =  t.decls.collect {
//        case m: c.universe.MethodSymbol if m.isCaseAccessor => (m.name, m.typeSignature)
//      }.toList
//
//      q"..$fields"
//    }

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
  }

//  def extractValues[A](a: A) = macro extractValues_impl[A]
//
//  def extractValues_impl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]) = {
//    import c.universe._
//
//    val paramRep = show(a.tree)
//    val paramRepTree = Literal(Constant(paramRep))
//    val paramRepExpr = c.Expr[String](paramRepTree)
//
//    val tpe = weakTypeOf[A]
//    val companion = tpe.typeSymbol.companionSymbol
//
//    val fields = tpe.declarations.collectFirst {
//      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
//    }.get.paramss.head
//
//    val toMapParams = fields.map { field =>
//      val name = field.name
//      val mapKey: String = name.decoded
//      q"$mapKey -> ${paramRepExpr}.${name.toTermName}"
//    }
//
//    c.Expr(q"..$toMapParams")
//  }
//
//  def caseToMap[A](a: A): Map[String, Any] = macro caseToMap_impl[A]
//
//  def caseToMap_impl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A]) = {
//    import c.universe._
//
//    val paramRep = show(a.tree)
//    val paramRepTree = Literal(Constant(paramRep))
//    val paramRepExpr = c.Expr[String](paramRepTree)
//
//    val tpe = weakTypeOf[A]
//    val companion = tpe.typeSymbol.companionSymbol
//
//    val fields = tpe.declarations.collectFirst {
//      case m: MethodSymbol if m.isPrimaryConstructor ⇒ m
//    }.get.paramss.head
//
//    ()
//
//    val toMapParams = fields.map { field =>
//      val name = field.name
//      val mapKey: String = name.decoded
//      q"$mapKey -> ${paramRepExpr}.${name.toTermName}"
//    }
//
//    c.Expr(q""" Map(..$toMapParams) """)
//  }

}
