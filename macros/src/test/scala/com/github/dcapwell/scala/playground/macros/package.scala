package com.github.dcapwell.scala.playground

import java.util.Date

package object macros {

  case class User(name: String, age: Int, sex: Char)

  case class Foo(name: String, desc: String)

  case class Event(name: String, createTS: Date, updateTS: Date)
}
