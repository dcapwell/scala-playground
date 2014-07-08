package macros

import compiler.Compiler
import org.scalatest.{FreeSpecLike, Matchers}

import scala.tools.reflect.ToolBoxError

class CaseClassTest extends FreeSpecLike with Matchers {
  import Macros.stringify

  val compiler = Compiler.DefaultCompiler

  "basic compiler check" in {
    import macros.caseClass

    @caseClass class Foo(foo: String, bar: String)
    val foo = new Foo("foo", "bar")
    foo.foo shouldBe "foo"
    foo.bar shouldBe "bar"
  }

  "stringify case class" in {
    val code = stringify {
      import macros.caseClass

      @caseClass class Foo(foo: String, bar: String)
      val foo = new Foo("foo", "bar")
      foo.foo
      foo.bar
    }

    println(code)
  }

  "compile case class" in {
    val code = """
      import macros.caseClass

      @caseClass class Foo(bar: String, val foobar: String = "fobar") {
        val biz: String = "biz"
        def foo: String = "foo"
      }
               """

    compiler eval code
  }

  "compile case class no members" in {
    val code = """
      import macros.caseClass

      @caseClass class Foo
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

  "wither case class stringify for debugging" in {
    compiler.eval(
      """
        |import macros.Macros.stringify
        |stringify {
        |      import macros.wither
        |
        |      @wither case class Foo(foo: String, bar: String)
        |
        |      val foo = Foo("foo", "bar")
        |      foo withFoo "foo 2"
        |    }
      """.stripMargin)
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
