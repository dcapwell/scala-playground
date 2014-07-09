package com.github.dcapwell.scala.playground
package macros

import org.scalatest.{FreeSpecLike, Matchers}

import scala.concurrent.{Await, Future}

/**
 * These tests are to play around with logging powered by macros.macros
 */
class TracingTest extends FreeSpecLike with Matchers {
  import Macros._

  val compiler = new Compiler(initialCommands = List("import logging._", "import Logger._"))

  "trace calling at the method level" in {
    def longRunning: String = trace {
      trace {
        (1 to 20) foreach { i =>
          i + 20
        }
      }
      "should be picked up"
    }

    longRunning
  }

  "partial function tracing" in {
    val fn: PartialFunction[Int, Int] = trace {
      case 1 => 1
      case 2 => 2
    }

    fn(1)
    fn(2)
  }

  "raw calling at the method level" in {
    val tree: String = raw {
      val tree = raw {
        (1 to 20) foreach { i =>
          i + 20
        }
      }
      println(tree)
    }

    Logger.info(tree)
  }

  "partial function raw" in {
    val tree: String = raw ({
      case 1 => 1
      case 2 => 2
    }: PartialFunction[Int, Int])

    val output = Logger.info(tree)
  }

  "partial function raw compiled" in {
    val code = stringify {
      val tree: String = raw ({
        case 1 => 1
        case 2 => 2
      }: PartialFunction[Int, Int])
      Logger.info(tree)
    }
    Logger.info(code)
    compiler eval code
//    val output = compiler.eval(
//      """
//        |val tree: String = raw ({
//        |      case 1 => 1
//        |      case 2 => 2
//        |    }: PartialFunction[Int, Int])
//        |info(tree)""".stripMargin)
  }

  import scala.concurrent.ExecutionContext.Implicits._
  import scala.concurrent.duration._

  "trace future" in {
    val f: Future[Int] = trace(Future(1))
    val data = Await.result(f, 1.second)
    data shouldBe 1
  }

  "raw future" in {
    val data = raw(Future(1))
    println(data)
  }

  "trace apply future" in {
    val f: Future[Int] = traceApply(Future(1))
    val data = Await.result(f, 1.second)
    data shouldBe 1
  }

  "trace apply future in compiler" in {
    val code = stringify {
      import scala.concurrent.ExecutionContext.Implicits._
      traceApply(scala.concurrent.Future(1))
    }
    Logger.info(code)
    compiler eval code
  }

  "trace work in apply future in compiler" in {
    val code = stringify {
      import scala.concurrent.ExecutionContext.Implicits._
      traceApply(scala.concurrent.Future[Int] {
        "some work"
        1
      })
    }
    Logger.info(code)
    compiler eval code
  }

  "inspect class creation" in {
    val code = stringify {
      case class Biz(you: String, should: Boolean)
    }

    debug(code)
  }
}
