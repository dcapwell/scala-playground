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
