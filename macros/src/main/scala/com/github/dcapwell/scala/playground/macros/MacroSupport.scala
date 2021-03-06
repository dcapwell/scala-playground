package com.github.dcapwell.scala.playground.macros

import scala.reflect.ClassTag
import scala.reflect.macros.{blackbox, whitebox}

trait TreeLenses { self: BlackboxSupport =>
  import c.universe._
  import scalaz.Lens

  val modFlags = Lens.lensu[Modifiers, FlagSet](
    set = (m, flags) => Modifiers(flags, m.privateWithin, m.annotations),
    get = (m) => m.flags
  )

  val valMods = Lens.lensu[ValDef, Modifiers](
    set = (v, m) => ValDef(m, v.name, v.tpt, v.rhs),
    get = (v) => v.mods
  )

  val valFlags: Lens[ValDef, FlagSet] = valMods andThen modFlags

  val defMods = Lens.lensu[DefDef, Modifiers](
    set = (d, m) => DefDef(m, d.name, d.tparams, d.vparamss, d.tpt, d.rhs),
    get = (d) => d.mods
  )

  val defFlags: Lens[DefDef, FlagSet] = defMods andThen modFlags

  val templBody = Lens.lensu[Template, List[Tree]](
    set = (t, b) => Template(t.parents, t.self, b),
    get = (t) => t.body
  )

  val templParents = Lens.lensu[Template, List[Tree]](
    set = (t, p) => Template(p, t.self, t.body),
    get = (t) => t.parents
  )

  val clazzTempl = Lens.lensu[ClassDef, Template](
    set = (c, t) => ClassDef(c.mods, c.name, c.tparams, t),
    get = (c) => c.impl
  )

  val clazzName = Lens.lensu[ClassDef, TypeName](
    set = (c, n) => ClassDef(c.mods, n, c.tparams, c.impl),
    get = (c) => c.name
  )

  val clazzMods = Lens.lensu[ClassDef, Modifiers](
    set = (c, m) => ClassDef(m, c.name, c.tparams, c.impl),
    get = (c) => c.mods
  )

  val clazzFlags = clazzMods andThen modFlags

  val clazzBody: Lens[ClassDef, List[Tree]] = clazzTempl andThen templBody

  val clazzParents: Lens[ClassDef, List[Tree]] = clazzTempl andThen templParents
}

trait TreeSupport extends TreeLenses { self: BlackboxSupport =>
  import c.universe._

  def isLiteral(tree: Tree): Boolean = tree match {
    case Literal(Constant(_)) => true
    case _ => false
  }

  def typeTree[A: TypeTag] = TypeTree(typeOf[A])

  val UnitLiteral = Literal(Constant(()))

  val MapSelf: Tree =>? Tree = {
    case t => t
  }

  implicit class TreeOps(self: Tree) {
    def findAll[T: ClassTag] : List[T] = self collect { case t: T => t }
  }

  implicit class ListTreeOps(self: List[Tree]) {
    def trees[T: ClassTag] : List[T] = self collect { case t: T => t }

    def mapPartial(pf: Tree =>? Tree): List[Tree] =
      self.map(pf orElse MapSelf)
  }

}

trait TypeSupport { self: BlackboxSupport =>
  import c.universe._

  def isCaseClass(t: Type): Boolean = {
    val sym = t.typeSymbol
    sym.isClass && sym.asClass.isCaseClass
  }

  def assertCaseClass(t: Type): Unit =
    if(!isCaseClass(t)) c.abort(c.enclosingPosition, s"${t.typeSymbol} is not a case class")

  def primaryConstructor(t: Type): MethodSymbol =
    t.decls.collectFirst { case m: MethodSymbol if m.isPrimaryConstructor => m }.getOrElse(c.abort(c.enclosingPosition, "Unable to find primary constructor for product"))

  def companionObject(t: Type): Symbol =
    t.typeSymbol.companion

  def caseFields(t: Type): List[Symbol] =
    primaryConstructor(t).paramLists.head

  def is(tpe: Type, typeString: String): Option[String] = {
    // expected type
    val typeSymbol = c.mirror.staticClass(typeString).asType
    val typeSymbolParams = typeSymbol.typeParams

    // given type
    val givenSymboles = typeSymbols(tpe)

    if(typeSymbolParams.size != givenSymboles.size) Some(s"Arity does not match; given ${toTypeString(tpe.typeSymbol, givenSymboles)}, but expected ${toTypeString(typeSymbol, typeSymbolParams)}")
    else {
      val typeCreated = typeSymbol.toType.substituteSymbols(typeSymbolParams, givenSymboles)


      if(! (tpe =:= typeCreated)) Some(s"Expected type is $typeCreated but was given $tpe")
      else None
    }
  }

  def typeSymbols(tpe: Type): List[Symbol] =
    tpe.typeArgs.map(_.typeSymbol.asType)
}

trait SymbolSupport { self: BlackboxSupport =>
  import c.universe._

  def toTypeString(clazz: Symbol, args: List[Symbol]): String = {
    if(args.isEmpty) clazz.toString
    else s"$clazz[${args.map(_.name).mkString(",")}]"
  }
}

trait ContextSupport { self: BlackboxSupport =>
  def abort(msg: String) =
    c.abort(c.enclosingPosition, msg)
}

trait BlackboxSupport extends TreeSupport with TypeSupport with SymbolSupport with ContextSupport {
  val c: blackbox.Context
}

trait WhiteboxSupport extends BlackboxSupport {
  val c: whitebox.Context
}