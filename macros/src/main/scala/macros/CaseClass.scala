package macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class CaseClassMacros(val c: whitebox.Context) {

  import c.universe._
  import c.universe.Flag._

  /**
   *  {{{ stringify { case class Foo(bar: String) } }}}
   *  should generate
   * {{{
   *   {
  case class Foo extends AnyRef with Product with Serializable {
    <caseaccessor> <paramaccessor> private[this] val bar: String = _;
    <stable> <caseaccessor> <accessor> <paramaccessor> def bar: String = Foo.this.bar;
    def <init>(bar: String): Foo = {
      Foo.super.<init>();
      ()
    };
    <synthetic> def copy(bar: String = bar): Foo = new Foo(bar);
    <synthetic> def copy$default$1: String = Foo.this.bar;
    override <synthetic> def productPrefix: String = "Foo";
    <synthetic> def productArity: Int = 1;
    <synthetic> def productElement(x$1: Int): Any = x$1 match {
      case 0 => Foo.this.bar
      case _ => throw new IndexOutOfBoundsException(x$1.toString())
    };
    override <synthetic> def productIterator: Iterator[Any] = runtime.this.ScalaRunTime.typedProductIterator[Any](Foo.this);
    <synthetic> def canEqual(x$1: Any): Boolean = x$1.$isInstanceOf[Foo]();
    override <synthetic> def hashCode(): Int = ScalaRunTime.this._hashCode(Foo.this);
    override <synthetic> def toString(): String = ScalaRunTime.this._toString(Foo.this);
    override <synthetic> def equals(x$1: Any): Boolean = Foo.this.eq(x$1.asInstanceOf[Object]).||(x$1 match {
  case (_: Foo) => true
  case _ => false
}.&&({
  <synthetic> val Foo$1: Foo = x$1.asInstanceOf[Foo];
      Foo.this.bar.==(Foo$1.bar).&&(Foo$1.canEqual(Foo.this))
    }))
  };
  <synthetic> object Foo extends scala.runtime.AbstractFunction1[String,Foo] with Serializable {
    def <init>(): Foo.type = {
      Foo.super.<init>();
      ()
    };
    final override <synthetic> def toString(): String = "Foo";
    case <synthetic> def apply(bar: String): Foo = new Foo(bar);
    case <synthetic> def unapply(x$0: Foo): Option[String] = if (x$0.==(null))
      scala.this.None
    else
      Some.apply[String](x$0.bar);
    <synthetic> private def readResolve(): Object = Foo
  };
  ()
}
   * }}}
   */
  //TODO finish
  def caseClass(annottees: c.Expr[Any]*): Tree = {
    annottees map(_.tree) head match {
      case ClassDef(modifiers, typeName, typeDefs, Template(parents, self, body)) =>
        val newBody = body.collect {
          case ValDef(mods, name, tpt, rhs) if mods.hasFlag(PARAMACCESSOR) =>
            ValDef(Modifiers(PARAMACCESSOR | CASEACCESSOR), name, tpt, rhs)
          case e => e
        }


        ClassDef(modifiers, typeName, typeDefs, Template(parents, self, newBody))
      case _ => abort("Only classes can use @CaseClass")
    }
  }

  def wither(annottees: c.Expr[Any]*): Tree = {
    annottees.head.tree match {
      case c @ ClassDef(modifiers, typeName, typeDefs, Template(parents, self, body)) if modifiers.hasFlag(CASE) =>

        val withMethods = body collect {
          case ValDef(mods, name, tpt, rhs) if mods.hasFlag(CASEACCESSOR) =>
            val termName = TermName(s"with${name.toString.capitalize}")
            q""" def ${termName}($name: $tpt): ${c.name} = this.copy($name = $name) """
        }

        ClassDef(modifiers, typeName, typeDefs, Template(parents, self, body ::: withMethods))
      case _ => abort("Only case classes can have wither support")
    }
  }

  private def abort(msg: String) =
    c.abort(c.enclosingPosition, msg)

}

class CaseClass extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro CaseClassMacros.caseClass
}

class wither extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro CaseClassMacros.wither
}
