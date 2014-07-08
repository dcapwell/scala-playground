package macros

import compiler.Compiler
import org.scalatest.{FreeSpecLike, Matchers}

class CaseClassTest extends FreeSpecLike with Matchers {
  import Macros.stringify

  val compiler = Compiler.DefaultCompiler

  "stringify case class" in {
    val data = stringify {
      import macros.CaseClass

      @CaseClass class Foo(bar: String)
    }

    println(data)
  }

  "compile case class" in {
    val data = """
      import macros.CaseClass

      @CaseClass class Foo(bar: String)
               """

    compiler eval data
  }
}
