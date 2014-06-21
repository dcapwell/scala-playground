package macros

import language.experimental.macros

import scala.reflect.macros.Context

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

//  //TODO when switching to 2.11, switch to WhiteboxMacro
//  trait CaseClassMacroBox { self: Context =>
//    import c.u
//  }

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
