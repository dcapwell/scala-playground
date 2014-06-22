package macros

/**
 * Don't ever use this object.  Its only here to make copy/paste code to REPL easier
 */
object Scratch {

  import language.experimental.macros

  import scala.reflect.macros.whitebox

//  def captureParam_impl(c: whitebox.Context)(param: c.Expr[Any]): c.Expr[Unit] = {
//    import c.universe._
//
//    def walkTree(t: Tree, deepth: Int = 0): Unit = {
//      (0 to deepth - 1).foreach(_ => print("-"))
//      t match {
//        case Select(qualifier, name) => println(s"qualifier = $qualifier, name = $name")
//        case _ => println(t.getClass)
//      }
//      if(!t.children.isEmpty) t.children foreach(walkTree(_, deepth + 1))
//    }
//
//    println(s"Type of input is ${param.actualType}")
//
//    walkTree(param.tree)
//
//    c.Expr[Unit](Literal(Constant(())))
//  }
//
//  def captureParam(param: Any): Unit = macro captureParam_impl
//
//  val name = "bob"
//  captureParam("hi")
//  captureParam(name)

}
