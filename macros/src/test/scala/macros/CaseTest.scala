package macros

import macros.Case._
import org.scalatest.{Matchers, FreeSpecLike}
import compiler.Compiler

import scala.tools.reflect.ToolBoxError

class CaseTest extends FreeSpecLike with Matchers {
  val compiler = Compiler.DefaultCompiler

  "type of tuple matches" in {
    compiler eval
      """
        |import macros.Case._
        |matchType[Tuple3[Int, Int, Int]]
      """.stripMargin
  }

  "type of tuple does not matche" in {
    intercept[ToolBoxError] {
      compiler eval
        """
          |import macros.Case._
          |matchType[Tuple2[Int, Int]]
        """.stripMargin
    }.message.split("\n").drop(2).head shouldBe "Arity does not match; given List(class Int, class Int), but expected List(type T1, type T2, type T3)"
}


}
