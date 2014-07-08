package macros

import compiler.Compiler
import org.scalatest.{FreeSpecLike, Matchers}

import scala.tools.reflect.ToolBoxError

class CaseClassTest extends FreeSpecLike with Matchers {
  import Macros.stringify

  val compiler = Compiler.DefaultCompiler

  "stringify case class" in {
    val code = stringify {
      import macros.CaseClass

      @CaseClass class Foo(bar: String)
    }

    println(code)
  }

  "compile case class" in {
    val code = """
      import macros.CaseClass

      @CaseClass class Foo(bar: String, val foobar: String = "fobar") {
        val biz: String = "biz"
        def foo: String = "foo"
      }
               """

    compiler eval code
  }

  "compile case class no members" in {
    val code = """
      import macros.CaseClass

      @CaseClass class Foo
               """

    compiler eval code
  }

  "wither case class stringify" in {
    val code = stringify {
      import macros.wither

      @wither case class Foo(foo: String, bar: String)

      val foo = Foo("foo", "bar")
      foo withFoo "foo 2"
    }

    println(code)
  }

  "wither case class" in {
    val code =
      """
        |import macros.wither
        |
        |@wither case class Foo(foo: String)
      """.stripMargin

    compiler eval code
  }

  "wither case class update foo" in {
    val code =
      """
        |import macros.wither
        |
        |@wither case class Foo(foo: String)
        |
        |val foo = Foo("bar")
        |foo withFoo "foo"
      """.stripMargin

    compiler eval code
  }

  "wither for non case class" in {
    val code =
      """
        |import macros.wither
        |
        |@wither class Foo(foo: String)
      """.stripMargin

    intercept[ToolBoxError](compiler eval code)
  }
}
