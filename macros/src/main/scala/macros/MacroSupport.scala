package macros

import scala.reflect.macros.{blackbox, whitebox}

trait BlackboxSupport {
  val c: blackbox.Context

  import c.universe._

  def abort(msg: String) =
    c.abort(c.enclosingPosition, msg)

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

  val clazzBody: Lens[ClassDef, List[Tree]] = clazzTempl andThen templBody

  val clazzParents: Lens[ClassDef, List[Tree]] = clazzTempl andThen templParents
}

trait WhiteboxSupport extends BlackboxSupport{
  val c: whitebox.Context
}
