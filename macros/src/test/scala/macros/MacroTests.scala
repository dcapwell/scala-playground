package macros

import java.util.Date

import org.scalatest.{Matchers, FreeSpecLike}

case class User(name: String, age: Int, sex: Char)

case class Foo(name: String, desc: String)

case class Event(name: String, createTS: Date, updateTS: Date)

class MacroTests extends FreeSpecLike with Matchers {

  import Macros._

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
  }
}