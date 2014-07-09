package com.github.dcapwell.scala.playground
package macros

import org.scalatest.{Matchers, FreeSpecLike}

import scala.tools.reflect.ToolBoxError

class CaseTest extends FreeSpecLike with Matchers {

  import Macros.{debug, stringify}

  val compiler = Compiler.DefaultCompiler.addCommands("import com.github.dcapwell.scala.playground.macros._", "import Case._")

  "type of tuple matches" in {
    compiler eval
      """
        |matchType[Tuple3[Int, Int, Int]]
      """.stripMargin
  }

  "type of tuple does not matche" in {
    intercept[ToolBoxError] {
      compiler eval
        """
          |matchType[Tuple2[Int, Int]]
        """.stripMargin
    }.message.split("\n").drop(2).head shouldBe "Arity does not match; given class Tuple2[Int,Int], but expected class Tuple3[T1,T2,T3]"
  }

  "convert tuple to case class" in {
    val code = stringify {
      val data = ("foo", "bar")
      import macros.Case._
      // ignore intellij, this will compile
      fromTuple[Foo](data)
    }

    val foo = compiler eval code
    debug(foo)
  }

  "convert tuple using two case classes should not compile" in {
    val code = """
      val data = Foo("foo", "bar")

      fromTuple[Foo](data)
    """

    intercept[ToolBoxError] {
      compiler eval code
    }.message.split("\n").drop(2).head shouldBe "Arity does not match; given class Foo, but expected class Tuple2[T1,T2]"
  }

}
