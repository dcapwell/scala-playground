package macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class CaseClassMacros(val c: whitebox.Context) {

  import c.universe._
  import c.universe.Flag._

  /**
   *  Given a normal class {{{class Foo(foo: String, bar: String)}}}, create a case class that looks identical to
   *  {{{case class Foo(foo: String, bar: String)}}}
   */
  //TODO finish
  def caseClass(annottees: c.Expr[Any]*): Tree = {
    annottees map(_.tree) head match {
      case ClassDef(modifiers, typeName, typeDefs, Template(parents, self, body)) =>
        val newBody = body.map {
          case v @ ValDef(mods, _, _, _) if mods.hasFlag(PARAMACCESSOR) =>
            liftToCase(v)
          case expr => expr
        }

        val caseDefs = newBody.collect {
          case v @ ValDef(mods, _, _, _) if mods.hasFlag(PARAMACCESSOR) =>
            createCaseAccessor(typeName, v)
        }


        ClassDef(modifiers, typeName, typeDefs, Template(parents, self, newBody ::: caseDefs))
      case _ => abort("Only classes can use @CaseClass")
    }
  }

  /**
   * Creates the following
   *
   * {{{
   <synthetic> object Foo extends scala.runtime.AbstractFunction2[String,String,Foo] with Serializable {
    def <init>(): Foo.type = {
      Foo.super.<init>();
      ()
    };
    final override <synthetic> def toString(): String = "Foo";
    case <synthetic> def apply(foo: String, bar: String): Foo = new Foo(foo, bar);
    case <synthetic> def unapply(x$0: Foo): Option[(String, String)] = if (x$0.==(null))
      scala.this.None
    else
      Some.apply[(String, String)](scala.Tuple2.apply[String, String](x$0.foo, x$0.bar));
    <synthetic> private def readResolve(): Object = Foo
  }
   * }}}
   */
  private def createCompanionObject = ???

  /**
   * Converts the class into a case class.  This will case the class modifier to be added to the class
   * and all paramaccessors to also be case classes, marked private, and new def functions created to replace them
   */
  private def liftToCase(body: ClassDef) = ???

  /**
   * Converts a paramaccessor valdef and converts it into a caseaccessor valdef and defdef that delegates to it.
   *
   * Generates the following
   *
   * input (may be private or public)
   * {{{
    <paramaccessor> private[this] val foo: String = _;
   * }}}
   *
   * output
   * {{{
    <caseaccessor> <paramaccessor> private[this] val foo: String = _;
    <stable> <caseaccessor> <accessor> <paramaccessor> def foo: String = Foo.this.foo;
   * }}}
   *
   * NOTE.  This function doesn't check that the ValDef given is a paramaccessor, it assumes that was done already
   */
  private def liftToCase(param: ValDef): ValDef =
    ValDef(Modifiers(PARAMACCESSOR | LOCAL | PRIVATE), TermName(s"${param.name.toString} "), param.tpt, param.rhs)

  private def createCaseAccessor(className: TypeName, param: ValDef): DefDef =
    DefDef(Modifiers(STABLE), TermName(param.name.toString.trim), List(), List(), param.tpt, Select(This(className), param.name))

  /**
   * Creates a copy method for the case class.  This will follow how scala generates case class copy, which is a function
   * that has default values that point to the access functions
   *
   * Generates the following
   *
   * {{{
    <synthetic> def copy(foo: String = foo, bar: String = bar): Foo = new Foo(foo, bar);
    <synthetic> def copy$default$1: String = Foo.this.foo;
    <synthetic> def copy$default$2: String = Foo.this.bar;
   * }}}
   */
  private def createCopyMethod = ???

  /**
   * Scala makes case classes extend Product which defines the arity of the class.  This function will add that into
   * the class
   *
   * Generates the following
   * {{{
   * case class Foo extends ... with Product ...
   * ...
   *   override <synthetic> def productPrefix: String = "Foo";
    <synthetic> def productArity: Int = 2;
    <synthetic> def productElement(x$1: Int): Any = x$1 match {
      case 0 => Foo.this.foo
      case 1 => Foo.this.bar
      case _ => throw new IndexOutOfBoundsException(x$1.toString())
    };
    override <synthetic> def productIterator: Iterator[Any] = runtime.this.ScalaRunTime.typedProductIterator[Any](Foo.this);
   * }}}
   */
  //TODO should this also add ProductN based off the size?  That way a case class and a tuple are really the same thing
  private def injectProduct = ???

  /**
   * Case classes are immutable objects, so adding equals and hashcode should be safe.  This method will add equals
   * and hashcode to the class based off how scala would have
   *
   * Generates the following
   * {{{
   *   <synthetic> def canEqual(x$1: Any): Boolean = x$1.$isInstanceOf[Foo]();
    override <synthetic> def hashCode(): Int = ScalaRunTime.this._hashCode(Foo.this);
    override <synthetic> def toString(): String = ScalaRunTime.this._toString(Foo.this);
    override <synthetic> def equals(x$1: Any): Boolean = Foo.this.eq(x$1.asInstanceOf[Object]).||(x$1 match {
  case (_: Foo) => true
  case _ => false
}.&&({
      <synthetic> val Foo$1: Foo = x$1.asInstanceOf[Foo];
      Foo.this.foo.==(Foo$1.foo).&&(Foo.this.bar.==(Foo$1.bar)).&&(Foo$1.canEqual(Foo.this))
    }))
  };
   * }}}
   */
  private def createEquality = ???

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

class caseClass extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro CaseClassMacros.caseClass
}

class wither extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro CaseClassMacros.wither
}
