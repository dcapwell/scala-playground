package logging

import org.scalatest.{FreeSpecLike, Matchers}

/**
 * These tests are to play around with logging powered by macros
 */
class TracingTest extends FreeSpecLike with Matchers {
  import Logger.{trace, raw}

  val compiler = macros.Compiler.initialCommands("import logging._", "import Logger._")

  "trace calling at the method level" in {
    def longRunning: String = trace {
      trace {
        (1 to 20) foreach { i =>
          i + 20
        }
      }
      "should be picked up"
    }

    longRunning
  }

  "partial function tracing" in {
    val fn: PartialFunction[Int, Int] = trace {
      case 1 => 1
      case 2 => 2
    }

    fn(1)
    fn(2)
  }

  "raw calling at the method level" in {
    val tree: String = raw {
      val tree = raw {
        (1 to 20) foreach { i =>
          i + 20
        }
      }
      println(tree)
    }

    Logger.info(tree)
  }

  "partial function raw" in {
    val tree: String = raw ({
      case 1 => 1
      case 2 => 2
    }: PartialFunction[Int, Int])

    val output = Logger.info(tree)

//    val t = new Throwable().fillInStackTrace()
//    t.getStackTrace foreach println
  }

  "partial function raw compiled" in {
    val output = compiler.eval(
      """
        |val tree: String = raw ({
        |      case 1 => 1
        |      case 2 => 2
        |    }: PartialFunction[Int, Int])
        |info(tree)""".stripMargin)
  }
}
