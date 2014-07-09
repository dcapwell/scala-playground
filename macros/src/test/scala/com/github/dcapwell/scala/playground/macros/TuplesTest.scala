package macros

import java.util.UUID

import org.scalatest.{Matchers, FreeSpecLike}

/**
 * These tests are for prototyping how I want to manipulate tuples
 */
//class TuplesTest extends FreeSpecLike with Matchers {
//  import TuplesTest._
//  import Macros._
//
//  "add to head" in {
//    val name: String = "Hello World"
//    val tuple: (Int, String) = (38, "Bob")
//
//    val mergedTuple = name :: tuple
//
//    debug(mergedTuple)
//  }
//
//  "add to head with case class" in {
//    val user = User("Bob", 38, 'M')
//    val key = UUID.randomUUID()
//
//    val mergedTuple = key :: toTuple(user)
//    debug(mergedTuple)
//  }
//
//  "add to head with case class from implicits" in {
//    val user = User("Bob", 38, 'M')
//    val key = UUID.randomUUID()
//
//    val mergedTuple = key :: user
//    debug(mergedTuple)
//
//    val (keySplit, userSplit) = mergedTuple.split
//    debug(keySplit, userSplit)
//
//    val userRet = mergedTuple.toUser
//    debug(userRet)
//  }
//
//  "add 2 to head with case class from implicits" in {
//    val user = User("Bob", 38, 'M')
//    val key1 = UUID.randomUUID()
//    val key2 = "key2"
//
//    val mergedTuple = (key1, key2) ::: user
//    debug(mergedTuple)
//
//    val (keySplit, key2Split, userSplit) = mergedTuple.split
//    debug(keySplit, key2Split, userSplit)
//
//    val userRet = mergedTuple.toUser
//    debug(userRet)
//  }
//
//}
//
//object TuplesTest {
//  import Macros._
//
//  implicit class UserOpts(val self: User) extends AnyVal {
//    type Tupled = (String, Int, Char)
//    type ConsTupled[A] = (A, String, Int, Char)
//    type ConsTupled2[A, B] = (A, B, String, Int, Char)
//
//    def tuplefy: Tupled = toTuple(self)
//
//    def ::[B](b: B): ConsTupled[B] = b :: tuplefy
//
//    def :::[A, B](a: (A, B)): ConsTupled2[A, B] = a ::: tuplefy
//  }
//
//  implicit class ConsUserOpts[A](val self: UserOpts#ConsTupled[A]) extends AnyVal {
//    def toUser: User = fromTuple[User](self.tail)
//    def split: (A, User) = (self.head, toUser)
//  }
//
//  implicit class ConsUser2Opts[A, B](val self: UserOpts#ConsTupled2[A, B]) extends AnyVal {
//    def toUser: User = fromTuple[User](self.drop(2))
//    def split: (A, B, User) = (self.head, self._2, toUser)
//  }
//
//
//
//  implicit class Tuple2Opts[A, B](val self: (A, B)) extends AnyVal {
//    def ::[C](c: C): (C, A, B) = (c, self._1, self._2)
//  }
//
//  implicit class Tuple3Opts[A, B, C](val self: (A, B, C)) extends AnyVal {
//    def ::[D](d: D) = (d, self._1, self._2, self._3)
//    def :::[D, E](d: (D, E)): (D, E, A, B, C) = (d._1, d._2, self._1, self._2, self._3)
//  }
//
//  implicit class Tuple4Opts[A, B, C, D](val self: (A, B, C, D)) extends AnyVal {
//    def head: A = self._1
//    def tail: (B, C, D) = (self._2, self._3, self._4)
//  }
//
//  implicit class Tuple5Opts[A, B, C, D, E](val self: (A, B, C, D, E)) extends AnyVal {
//    def head: A = self._1
//    def tail: (B, C, D, E) = (self._2, self._3, self._4, self._5)
//    def drop(n: Int) = {
//      assume(n == 2, "Only support drop 2 at the moment")
//      (self._3, self._4, self._5)
//    }
//
//    // def dropRight(n: Int)
//    // def take(n: Int)
//    // def takeRight(n: Int)
//  }
//}
