package macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class CaseClassMacros(val c: whitebox.Context) {

  import c.universe._

  def caseClass(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val inputs = annottees map(_.tree) toList
    val (annottee, expandees) = inputs match {
      case (param: ValDef) :: (rest @ (_ :: _)) => (param, rest)
      case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
      case _ => (EmptyTree, inputs)
    }

    val outputs = expandees

    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }

}

class CaseClass extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro CaseClassMacros.caseClass
}
