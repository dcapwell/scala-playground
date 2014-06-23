package playground.macros.test

import compiler.Compiler
import macros.Macros

trait EvalMacro { self: Compiler =>
  def eval(body: => Any): Any =
    self.eval(Macros.stringify(body))
}
