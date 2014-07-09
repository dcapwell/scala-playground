package macros

import scala.language.experimental.macros
import scala.language.postfixOps
import scala.annotation.StaticAnnotation
import scala.reflect.macros.blackbox

class CaseClassMacros(val c: blackbox.Context) extends BlackboxSupport {

  import c.universe._
  import c.universe.Flag._

  /**
   *  Given a normal class {{{class Foo(foo: String, bar: String)}}}, create a case class that looks identical to
   *  {{{case class Foo(foo: String, bar: String)}}}
   */
  //TODO finish
  def caseClass(annottees: c.Expr[Any]*): Tree = {
    annottees.head.tree match {
      case cd: ClassDef => createCopyMethod(injectProduct(liftToCase(cd))) //TODO any monad that makes this cleaner?
      case _ => abort("Only classes can use @CaseClass")
    }
  }

  /**
   * Converts the class into a case class.  This will case the class modifier to be added to the class
   * and all paramaccessors to also be case classes, marked private, and new def functions created to replace them
   */
  private def liftToCase(clazz: ClassDef): ClassDef = {
    val typeName = clazz.name
    val newBody = clazz.impl.body.mapPartial {
      case v @ ValDef(mods, _, _, _) if mods.hasFlag(PARAMACCESSOR) =>
        ValDef(Modifiers(PARAMACCESSOR | LOCAL | PRIVATE), TermName(s"${v.name.toString} "), v.tpt, v.rhs)
    }

    val caseDefs = newBody.trees[ValDef] withFilter(_.mods.hasFlag(PARAMACCESSOR)) map{ v =>
      DefDef(Modifiers(STABLE | CASEACCESSOR), TermName(v.name.toString.trim), List(), List(), v.tpt, Select(This(typeName), v.name))
    }

    clazzFlags.mod(_ | CASE, clazzBody.set(clazz, newBody ::: caseDefs))
  }

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
  private def createCopyMethod(clazz: ClassDef): ClassDef = {
    val params = clazz.impl.body.trees[ValDef].filter(_.mods.hasFlag(PARAMACCESSOR))

    val paramNames = params.map(_.name)
    val argsWithDefault: List[ValDef] = params.map{p =>
      q"""val ${p.name}: ${p.tpt} = ${p.name} """.asInstanceOf[ValDef]
    }

    val copy = addMod(q""" def copy(..$argsWithDefault): ${clazz.name} = new ${clazz.name}(..$paramNames); """, SYNTHETIC)

    clazzBody.mod(_ :+ copy, clazz)
  }

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
  private def injectProduct(clazz: ClassDef): ClassDef = {
    val params = clazz.impl.body.collect{case v @ ValDef(mods, _, _, _) if mods.hasFlag(PARAMACCESSOR) => v}

    def productPrefix: Tree = {
      val classStr = clazz.name.toString
      addMod( q"""override def productPrefix: String = $classStr; """, SYNTHETIC)
    }

    def productArity: Tree = {
      val size = params.size
      addMod( q"""override def productArity: Int = $size; """, SYNTHETIC)
    }

    def productElement: Tree = {
      var i = 0
      val elementCases = params.map{p =>
        val pat = pq"$i"
        val cd = cq"""$pat => Foo.this.${p.name}"""
        i = i + 1 // zipWithIndex is giving me issues since params is ValDef and I return a Tree....  YAY CanBuildFrom!
        cd
      }

      addMod(q"""override def productElement(x: Int): Any = x match {
                                case ..$elementCases
                                case _ => throw new IndexOutOfBoundsException(x.toString())
                              } """, SYNTHETIC)
    }

    def productIterator: Tree = {
      val paramNames = params.map(_.name)
      addMod(q"""override def productIterator: Iterator[Any] = Iterator(..$paramNames) """, SYNTHETIC)
    }

    def canEqual: Tree = {
      addMod(q"""override def canEqual(x: Any): Boolean = x.isInstanceOf[${clazz.name}] """, SYNTHETIC)
    }

    def withProductDefs(clazz: ClassDef): ClassDef =
      clazzBody.mod(_ ::: List(productPrefix, productArity, productElement, productIterator, canEqual), clazz)

    def withParent(clazz: ClassDef, parents: List[Tree]): ClassDef =
      clazzParents.mod(_ ::: parents, clazz)

    val product = typeTree[Product]
    val newClazz = withProductDefs(withParent(clazz, List(product)))
    newClazz
  }

  private def addMod(tree: Tree, fs: FlagSet) = tree match {
    case d: DefDef => defFlags.mod(mods => mods | fs, d)
  }

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

}

class caseClass extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro CaseClassMacros.caseClass
}

class wither extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro CaseClassMacros.wither
}
