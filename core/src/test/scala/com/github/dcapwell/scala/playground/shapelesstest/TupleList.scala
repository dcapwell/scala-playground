package com.github.dcapwell.scala.playground.shapelesstest

import org.scalatest.{FreeSpecLike, Matchers}
import shapeless.syntax

class TupleList extends FreeSpecLike with Matchers {
  import shapeless.syntax.std.tuple._

  "head in tuple" in {
    (1, 2, 3).head shouldBe 1
  }

  "tail" in {
    (1, 2, 3).tail shouldBe (2, 3)
  }

  "drop" in {
    (1, 2, 3).drop(1) shouldBe (2, 3)
  }

  "take" in {
    (1, 2, 3).take(2) shouldBe (1, 2)
  }

  "split" in {
    (1, 2, 3, 4).split(2) shouldBe((1, 2), (3, 4))
  }

  "add" in {
    1 +: (2, 3, 4) shouldBe (1, 2, 3, 4)

    (1, 2, 3) :+ 4 shouldBe (1, 2, 3, 4)

    (1, 2) ++ (3, 4) shouldBe (1, 2, 3, 4)

//    1 ::: (2, 3, 4) shouldBe (1, 2, 3, 4)

    (1, 2) ::: (3, 4) shouldBe (1, 2, 3, 4)
  }

  "element" in {
    // apply is done as a macro, hence why IDEA thinks it doesn't compile.  This works just fine
    val mixed = (1, "two", '3')
    val one: Int = mixed(0)
    val two: String = mixed(1)
    val three: Char = mixed(2)
  }
}
