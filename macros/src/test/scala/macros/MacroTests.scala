package macros

import java.io.File
import java.util.Date

import compiler.Compiler

import org.scalatest.{Matchers, FreeSpecLike}

import scala.tools.reflect.ToolBoxError

class MacroTests extends FreeSpecLike with Matchers {
  import macros.Macros._

  val compiler = new Compiler(initialCommands = List("import macros._", "import Macros._"))

  "Hello macro prints hello world" in {
    hello()
  }

  "Printparams printslns with arguments" in {
    printparam("foo")
    printparam(1 + 3)
  }

  "Debug1 prints out the expression passed in" in {
    val valueName = "value result"
    debug1(valueName)

    debug1("hellow")

    debug1(1 + 3)
  }

  "debug statement prints out expressions" in {
    val name = "john"
    val age = 32
    val sex = 'M'

    debug("The user", (name, age, sex), "was created at", new Date())
    debug("The user", User(name, age, sex), "was created at", new Date())
  }

  "sdebug statement prints out expressions" in {
    val name = "john"
    val age = 32
    val sex = 'M'

    val out1 = sdebug("The user", (name, age, sex), "was created at", new Date())
    debug(out1)

    val out2 = sdebug("The user", User(name, age, sex), "was created at", new Date())
    debug(out2)
  }

  "extract type" in {
    debug("Checking users ", caseFields[User])
    debug("checking strings ", caseFields[String])
    debug("checking ints ", caseFields[Int])
    debug("checking list but with type alias", caseFields[({ type V = List[Int] })#V])
  }

  "expr toString" in {
    debug(stringify {
      "this is just a test"
      println("to see how good")
    })

    debug(stringify(debug("hi")))

    debug(stringify("hi"))

    val foo = "bar"
    debug(stringify(foo))

    debug(stringify(debug(stringify(foo))))
  }

  "create object" in {
    trait FooBar {
      println("created")
    }

    val foobar = createFromTrait("FooBar")

    debug(foobar)
  }

  "case name" in {
    val data = extractCaseName[User]

    debug(data)
  }

  "case to map" in {
    val user = User("bob", 38, 'M')

    val userMap = toMap(user)
    // intellij says this doesn't compile, but it will after macro expantion
    val fooMap: Map[String, String] = toMap(Foo("bar", "baz"))

    debug(userMap)
    debug(fooMap)

    debug(fromMap[User](userMap))
    debug(fromMap[Foo](fooMap))

    // can't know at compile time that this won't work, since value type is any
//    intercept[Throwable] {
//      compiler.eval(
//        """
//          |val fooMap: Map[String, String] = Map("name" -> "name", "desc" -> "desc")
//          |fromMap[User](fooMap)""".stripMargin)
//    }.getMessage.split("\n").drop(2).head shouldBe "key not found: age"
  }

  "case to tuple" in {
    // compile error if you don't give the types, since scala defaults to Nothing
//    debug(toTuple[User,(String, Int, Char)](User("bob", 38, 'M')))

    // if we set it to any, the type will still be (String, Int, Char)
//    debug(toTuple[User,Any](User("bob", 38, 'M')))

    def sic(input: (String, Int, Char)) = input

    debug(sic(toTuple(User("bob", 38, 'M'))))
//    debug(sic(toTuple(Foo("bar", "baz"))))
  }

  "tuple to case" in {
    val user = compiler.eval(
      """
        |import macros.Macros._
        |fromTuple[User]("bob", 38, 'm')""".stripMargin)

    debug(user)
  }

  "eval hello" in {
    val output = compiler.eval("""
        |hello()
      """.stripMargin)

    debug(output)
  }

  "parse hello" in {
    val output = compiler.parse("""
                        |hello()
                      """.stripMargin)

    debug(output)
  }

  "tuple to case of different shape" in {
    intercept[ToolBoxError] {
      compiler.eval("""
                      |case class User(name: String, age: Int, sex: Char)
                      |
                      |fromTuple[User]("bob", "hi")
                    """.stripMargin)
    }.getMessage.split("\n").drop(2).head shouldBe "Expected type is scala.Tuple3[String, Int, Char], but given (String, String)"
  }


  "pimp my lib" in {
    val code = stringify {
      import Files._

      val doesNotExistOpt = Files.Tmp / "foo" / "bar" / "baz" toOption

      doesNotExistOpt shouldBe None
    }

    debug(code)
  }
}

object Files {
  /**
   * Root file on the operating system
   */
  lazy val Root = new File("/")

  /**
   * Location of tmp on the system
   */
  lazy val Tmp = new File(System.getProperty("java.io.tmpdir", "/tmp"))

  implicit class FileOpts(val file: File) extends AnyVal {
    def /(child: String): File = new File(file, child)

    def deleteAll: Boolean = {
      if (file.isDirectory) for {p: File <- file.listFiles()} p.deleteAll
      file.delete()
    }

    def toOption: Option[File] =
      if(file.exists()) Some(file) else None

    def children: List[File] = file.listFiles() match {
      case null => List()
      case e => e.toList
    }
  }
}