package macros

import java.util.Date

import org.scalatest.{Matchers, FreeSpecLike}

case class User(name: String, age: Int, sex: Char)

case class Event(name: String, createTS: Date, updateTS: Date)

class MacroTests extends FreeSpecLike with Matchers {

  import DebugMacros._

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

  "extract type" in {
    debug("Checking users ", extractName[User])
    debug("checking strings ", extractName[String])
    debug("checking ints ", extractName[Int])
    debug("checking list but with type alias", extractName[({ type V = List[Int] })#V])
  }

  "create object" in {
    trait FooBar {
      println("created")
    }

    val foobar = createFromTrait("FooBar")

    debug(foobar)
  }

//  "case class to map" in {
//    val user = User("Bob", 32, 'M')
//    val map = caseToMap(user)
//
//    debug(user, map)
//  }

}

//object Test extends App {
//  import DebugMacros._
//
//  val user = User("Bob", 32, 'M')
//  val map = extractValues(user)
//
//  debug(user, map)
//}
