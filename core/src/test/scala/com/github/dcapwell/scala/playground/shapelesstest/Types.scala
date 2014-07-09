package com.github.dcapwell.scala.playground.shapelesstest

import org.scalatest.{FreeSpecLike, Matchers}
import shapeless._

class Types extends FreeSpecLike with Matchers {
  import shapeless.syntax.singleton._

  "int 23" in {
    var foo = 23.narrow
//    foo = 24 // won't compile, Int(24) doesn not match type Int(23)
//    Error:(10, 11) type mismatch;
//    found   : Int(24)
//    required: Int(23)
//      foo = 24
//          ^
  }

  "string foo" in {
    var foo = "foo".narrow
//    foo = "bar" // won't compile, String("bar") not same type as String("foo")
//    Error:(20, 11) type mismatch;
//    found   : String("bar")
//    required: String("foo")
//      foo = "bar"
//          ^
  }

  "path dependent types + singleton types" in {
    val (wTrue, wFalse) = (Witness(true), Witness(false))

    type True = wTrue.T

    type False = wFalse.T

    trait Select[B] { type Out }

    implicit val selInt = new Select[True] { type Out = Int }

    implicit val selString = new Select[False] { type Out = String }
    def select[T](b: WitnessWith[Select])(t: b.Out) = t

    select(true)(23)

//    select(true)("foo")
//    Error:(44, 18) type mismatch;
//    found   : String("foo")
//    required: Int
//      select(true)("foo")
//                ^

//    select(false)(23)
    //    Error:(46, 19) type mismatch;
    //    found   : Int(23)
    //    required: String
    //      select(false)(23)
    //               ^

    select(false)("foo")
  }

  "replace case classes with hlist" in {
    import shapeless.record._
    import shapeless.syntax.singleton._

    val book =
      ("author" ->> "Benjamin Pierce") ::
        ("title"  ->> "Types and Programming Languages") ::
        ("id"     ->>  262162091) ::
        ("price"  ->>  44.11) ::
        HNil

    val auth: String = book("author")  // Note result type ...

    val title: String = book("title")   // Note result type ...

    val id: Int = book("id")      // Note result type ...

    val price: Double = book("price")   // Note result type ...

    book.keys       // Keys are materialized from singleton types encoded in value type
    // String("author") :: String("title") :: String("id") :: String("price") :: HNil = author :: title :: id :: price :: HNil

    book.values
    // String :: String :: Int :: Double :: HNil = Benjamin Pierce :: Types and Programming Languages :: 262162091 :: 44.11 :: HNil

    val newPrice = book("price")+2.0

    val updated = book +("price" ->> newPrice)  // Update an existing field
    // Benjamin Pierce :: Types and Programming Languages :: 262162091 :: 46.11 :: HNil

    updated("price")
    // Double = 46.11

    val extended = updated + ("inPrint" ->> true)  // Add a new field
    // Benjamin Pierce :: Types and Programming Languages :: 262162091 :: 46.11 :: true :: HNil

    val noId = extended - "id"  // Removed a field
    // Benjamin Pierce :: Types and Programming Languages :: 46.11 :: true :: HNil

//    noId("id")  // Attempting to access a missing field is a compile time error
//    <console>:25: error: could not find implicit value for parameter selector ...
//      noId("id")
//      ^
  }

  "Coproducts and discriminated unions" in {
    type ISB = Int :+: String :+: Boolean :+: CNil
    val isb = Coproduct[ISB]("foo")

    isb.select[Int] shouldBe None
    isb.select[String] shouldBe Some("foo")

    // polymorphic function, supports different input types
    object size extends Poly1 {
      implicit def caseInt = at[Int](i => (i, i))
      implicit def caseString = at[String](s => (s, s.length))
      implicit def caseBoolean = at[Boolean](b => (b, 1))
    }

    val mapped = isb map size
    // (Int, Int) :+: (String, Int) :+: (Boolean, Int) :+: CNil = (foo,3)

    mapped.select[(String, Int)] shouldBe Some("foo" -> 3)
  }
}
