package logging

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait LoggerFunctions {
  self =>

  def info(msg: Any): Unit = macro LoggerMacros.info

  def trace[A](param: => A): A = macro LoggerMacros.trace[A]

  def traceApply[A](param: => A): A = macro LoggerMacros.traceApply[A]

  def raw(param: => Any): String = macro LoggerMacros.raw

  def stringify(param: => Any): String = macro LoggerMacros.stringify
}

object Logger extends LoggerFunctions

class LoggerMacros(val c: blackbox.Context) {

  import c.universe._

  def info(msg: c.Expr[Any]): c.Expr[Unit] = {
    def walkOwner(s: Symbol): List[Symbol] = s match {
      case NoSymbol => List()
      case o => o :: walkOwner(s.owner)
    }

    val caller = walkOwner(c.enclosingClass.symbol).reverse.map(_.name.toString).filterNot(_ == "<root>").mkString(".")
    val encPos = c.enclosingPosition

    c.Expr[Unit]( q"""java.lang.System.out.println("INFO  [2014-06-23 04:54:10,804] " + ${caller} + ":{" + ${encPos.line} + ":" + ${encPos.column} + "} " + $msg) """)
  }

  def stringify(param: c.Expr[Any]): c.Expr[String] = {
    c.Expr[String](Literal(Constant(show(param.tree))))
  }

  def trace[A: c.WeakTypeTag](param: c.Expr[A]): c.Expr[A] = {
    val body = c.Expr[String](Literal(Constant(show(param.tree))))
    c.Expr[A](timeTree(param.tree))
  }

  def raw(param: c.Expr[Any]): c.Expr[String] = {
    c.Expr[String](Literal(Constant(showRaw(param))))
  }

  private def timeTree(t: Tree): Tree = {
    val body = c.Expr[String](Literal(Constant(show(t))))
    q"""
    val startTime: Long = java.lang.System.nanoTime()
    val output = $t
    val totalTime: Long = java.lang.System.nanoTime() - startTime
    java.lang.System.out.println("[TRACE] Took " + totalTime + "ns; Ran expression " + $body)
    output
     """
  }

  def traceApply[A: c.WeakTypeTag](param: c.Expr[A]): c.Expr[A] = {
    val treeFn: PartialFunction[Tree, Tree] = {
      case q"scala.concurrent.Future.apply[$tpe]($body)($ec)" => body match {
        case b: Tree =>
          val body = timeTree(b)
          q"""scala.concurrent.Future.apply[$tpe]($body)($ec)"""
      }
      case _ => c.abort(c.enclosingPosition, "This method only works with Future apply")
    }

    val tree = treeFn(param.tree)

    c.Expr[A](tree)
  }

  //      object Walker extends Traverser {
  //        override def traverse(tree: Tree): Unit = tree match {
  ////          case  f @ Apply(
  ////            Apply(
  ////              TypeApply( Select( Select( Select( Ident("scala"), "scala.concurrent"), "scala.concurrent.Future"), TermName("apply")), List(TypeTree())), List(Literal(Constant(1)))),
  ////              List(
  ////                Select(Select(Select(Select(Ident("scala"), "scala.concurrent"), "scala.concurrent.ExecutionContext"), "scala.concurrent.ExecutionContext.Implicits"), TermName("global")))) =>
  //          case q"""scala.concurrent.Future($body) """ => c.abort(c.enclosingPosition, s"found future with body $body")
  //
  //          case _ => super.traverse(tree)
  //        }
  //      }
  //
  //      Walker.traverse(param.tree)

}
