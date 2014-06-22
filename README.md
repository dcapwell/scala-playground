scala-playground
================

Playing with scala

## REPL

To play around with macros in the REPL, just add the following at the start of `sbt console`

```scala
import language.experimental.macros
import scala.reflect.macros.{whitebox, blackbox}
import scala.annotation.StaticAnnotation
import scala.reflect.runtime.{universe => ru}
import ru._
```

Verify quasiquotes works

```scala
q"val x = 3"
```
And define a simple macro to prove that it macros works

```scala
def hello_impl(c: blackbox.Context)(): c.Expr[Unit] = {
  import c.universe._

  reify { println("Hello World") }
}

def hello(): Unit = macro hello_impl

hello()
```

## Refs

[Exploring scala macros map to case class conversion](http://blog.echo.sh/post/65955606729/exploring-scala-macros-map-to-case-class-conversion)

[Quasiquotes](http://docs.scala-lang.org/overviews/quasiquotes/intro.html)

[Quasiquotes for multiple paramater lists](http://meta.plasm.us/posts/2013/09/06/quasiquotes-for-multiple-parameter-lists/)

[Figi](https://github.com/ncreep/figi/blob/master/macros/src/main/scala/ncreep/figi/Figi.scala)

[Debugging scala macros](http://www.cakesolutions.net/teamblogs/2013/09/30/debugging-scala-macros)

[Debug macro](https://github.com/adamw/scala-macro-debug/blob/master/macros/src/main/scala/com/softwaremill/debug/DebugMacros.scala)

[Scala Lang Macro Doc](http://docs.scala-lang.org/overviews/macros/overview.html)

[Learning scala macros](http://imranrashid.com/posts/learning-scala-macros/)

[Quasiquotes for scala](http://infoscience.epfl.ch/record/185242/files/QuasiquotesForScala.pdf)

[parse](http://stackoverflow.com/questions/14790115/where-can-i-learn-about-constructing-asts-for-scala-macros/14795999#14795999)

[Symbols trees and types](http://docs.scala-lang.org/overviews/reflection/symbols-trees-types.html#trees)

[implicit class materialization ](https://groups.google.com/forum/#!topic/scala-user/uXqWJU0kbHs)
