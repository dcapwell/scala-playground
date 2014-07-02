package macros

import org.scalatest.{Matchers, FreeSpecLike}
import compiler.Compiler

import scala.tools.reflect.ToolBoxError

class CaseTest extends FreeSpecLike with Matchers {

  import Macros.{debug, stringify}

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

  "convert tuple to case class" in {

    val code = stringify {
      import macros.CaseTest._
      val data = ("foo", "bar")
      import macros.Case._
      // ignore intellij, this will compile
      fromTuple[Foo](data)
    }

    val foo = compiler eval code
    debug(foo)
  }

  "convert tuple using two case classes should not compile" in {
    val code = stringify {
      val data = Foo("foo", "bar")

      import macros.Case._
      fromTuple[Foo](data)
    }

    val foo = compiler eval code
    debug(foo)
  }

}

object CaseTest {
  // if you try to get the companion object when you put this in the
  // compiler block, it says its missing.  Make sure its compiled before
  // the test compiler kicks in
  case class Foo(name: String, value: String)
}
