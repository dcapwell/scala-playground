package com.github.dcapwell.scala.playground
package macros

import org.scalatest.{FreeSpecLike, Matchers}

import scala.tools.reflect.ToolBoxError

class CaseClassTest extends FreeSpecLike with Matchers {
  import Macros.stringify

  val compiler = Compiler.DefaultCompiler.addCommands("import com.github.dcapwell.scala.playground.macros._", "import Macros._")

  "basic compiler check" in {
    @caseClass class Foo(foo: String, bar: String, x: Int)
    val foo = new Foo("foo", "bar", 0)
    foo.foo shouldBe "foo"
    foo.bar shouldBe "bar"

    foo.productElement(0) shouldBe "foo"
    foo.productElement(1) shouldBe "bar"
    foo.productElement(2) shouldBe 0
    intercept[IndexOutOfBoundsException] {
      foo.productElement(3)
    }
  }

  "stringify case class" in {
    val code = stringify {
      @caseClass class Foo(foo: String, bar: String)
      val foo = new Foo("foo", "bar")
      val fooVal = foo.foo
      val barVal = foo.bar
    }

    println(code)
  }

  "compile case class" in {
    val code = """
      @caseClass class Foo(bar: String, val foobar: String = "fobar") {
        val biz: String = "biz"
        def foo: String = "foo"
      }
               """

    compiler eval code
  }

  "compile case class no members" in {
    val code = """
      @caseClass class Foo
               """

    compiler eval code
  }

  "wither case class stringify" in {
    val code = stringify {
      @wither case class Foo(foo: String, bar: String)

      val foo = Foo("foo", "bar")
      foo withFoo "foo 2"
    }

    println(code)
  }

  "wither case class stringify for debugging" in {
    compiler.eval(
      """
        |stringify {
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
        |@wither case class Foo(foo: String)
      """.stripMargin

    compiler eval code
  }

  "wither case class update foo" in {
    val code =
      """
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
        |@wither class Foo(foo: String)
      """.stripMargin

    intercept[ToolBoxError](compiler eval code)
  }
}
