package shapeless

import org.scalatest.{FreeSpecLike, Matchers}
import spire.algebra.Monoid

import scalaz.Monoid

case class Book(author: String, title: String, id: Int, price: Double)

case class ExtendedBook(author: String, title: String, id: Int, price: Double, inPrint: Boolean)

case class Address(street : String, city : String, postcode : String)

case class Person(name : String, age : Int, address : Address)

class CaseClasses extends FreeSpecLike with Matchers {
  import record._
  import syntax.singleton._

  "generator" in {
    val bookGen = LabelledGeneric[Book]

    val tapl = Book("Benjamin Pierce", "Types and Programming Languages", 262162091, 44.11)

    val rec = bookGen.to(tapl)
    // bookGen.Repr = Benjamin Pierce :: Types and Programming Languages :: 262162091 :: 44.11 :: HNil

    // ignore the intellij warnings, this will compile
    rec('price) shouldBe 44.11

    bookGen.from(rec.updateWith('price)(_+2.0))
    // Book = Book(Benjamin Pierce,Types and Programming Languages,262162091,46.11)

    val bookExtGen = LabelledGeneric[ExtendedBook]

    // create a extended book from a book, even though there is no inheritance
    bookExtGen.from(rec + ('inPrint ->> true))
    // ExtendedBook = ExtendedBook(Benjamin Pierce,Types and Programming Languages,262162091,44.11,true)
  }

  "lenses" in {
    // Some lenses over Person/Address ...
    val nameLens     = lens[Person] >> 'name
    val ageLens      = lens[Person] >> 'age
    val addressLens  = lens[Person] >> 'address
    val streetLens   = lens[Person] >> 'address >> 'street
    val cityLens     = lens[Person] >> 'address >> 'city
    val postcodeLens = lens[Person] >> 'address >> 'postcode


    val person = Person("Joe Grey", 37, Address("Southover Street", "Brighton", "BN2 9UA"))

    ageLens.get(person) shouldBe 37

    val person2 = ageLens.set(person)(38)
    val person3 = ageLens.modify(person2)(_ + 1)

    val street = streetLens.get(person3)
    val person4 = streetLens.set(person3)("Montpelier Road")
  }

  // doesn't compile even though its in the docs
  // https://github.com/milessabin/shapeless/wiki/Feature-overview:-shapeless-2.0.0#polymorphic-function-values
//  "even rocky had a monoid!" in {
//    import MonoidSyntax._
//    import Monoid.auto._
//
//
//
//    case class Foo(i : Int, s : String)
//    case class Bar(b : Boolean, s : String, d : Double)
//
//    Foo(13, "foo") |+| Foo(23, "bar")
//
////    val i = lens[Foo] >> 'i
////
////    i.get(result) shouldBe 36
////    val updatedFoo = i.set(result)(42)
////    i.get(updatedFoo) shouldBe 42
//  }

}
