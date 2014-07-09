package com.github.dcapwell.scala.playground.stm

import org.scalatest.{FreeSpecLike, Matchers}

import scala.concurrent.stm._

class STMTest extends FreeSpecLike with Matchers {

  "simple ref with view" in {
    val element: Ref[Int] = Ref(0)

    // views don't need locks
    val view = element.single
    view.get shouldBe 0
  }

  "simple ref update" in {
    val element: Ref[Int] = Ref(0)

    atomic { implicit tx =>
      element() = 1
      element() shouldBe 1
    }

    element.single.get shouldBe 1
  }

//  "update out of transaction" in {
//    val element = Ref(0)
//    element() = 1 // won't compile, needs implicit txn
//  }

  "function with tnx" in {
    val element: Ref[Int] = Ref(0)

    def ret(implicit txn: InTxn): Int = element()

    atomic { implicit txn =>
      ret
    }
  }

}
