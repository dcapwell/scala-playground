package logging

import language.experimental.macros

import scala.reflect.macros.blackbox

// format:
// INFO  [2014-06-23 04:54:10,804] $c.enclosingClass.name: $msg
trait LoggerFunctions { self =>

  def info(msg: Any): Unit = macro LoggerMacros.info

  def trace[A](param: => A): A = macro LoggerMacros.trace[A]

  def raw(param: => Any): String = macro LoggerMacros.raw
}

object Logger extends LoggerFunctions

class LoggerMacros(val c: blackbox.Context) {
  import c.universe._

  def info(msg: c.Expr[Any]): c.Expr[Unit] = {
    def walkOwner(s: Symbol): List[Symbol] =  s match {
      case NoSymbol => List()
      case o => o :: walkOwner(s.owner)
    }

    val caller = walkOwner(c.enclosingClass.symbol).reverse.map(_.name.toString).filterNot(_ == "<root>").mkString(".")
    val encPos = c.enclosingPosition


    c.Expr[Unit](q"""println("INFO  [2014-06-23 04:54:10,804] " + ${caller} + ":{" + ${encPos.line} + ":" + ${encPos.column} + "} " + $msg) """)
  }

  def trace[A: c.WeakTypeTag](param: c.Expr[A]): c.Expr[A] = {
    val body = c.Expr[String](Literal(Constant(show(param.tree))))
    c.Expr[A](q"""
    val startTime: Long = System.nanoTime()
    val output = $param
    val totalTime: Long = System.nanoTime() - startTime
    println("[TRACE] Took " + totalTime + "ns; Ran expression " + $body)
    output
     """)
  }

  def raw(param: c.Expr[Any]): c.Expr[String] = {
    c.Expr[String](Literal(Constant(showRaw(param))))
  }
}
