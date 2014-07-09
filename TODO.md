* Convert case class to: tuple, list, map
* Convert tuple/map to case class
* macro type that auto adds FunctionX based off apply (extends Fn)
* add "with" methods for each field

* Slick
  * auto build slick tables from case classes
  * macro type for forcing sql error checks
  * auto add session to all methods in tables object
  * auto generate join query utils
    * convert left/right side to option
    * 
  * get the following to work

```scala
type ClusterId = String
case class Cluster(id: ClusterId, name: String)

type RoleInstance = String
case class RoleInstance(id: RoleInstance, name: String)

// create a macro like the following: create a role_instance table with fk on Cluster by id
table[RoleInstance, Cluster)(_.id)

roleInstances.add(clusterId)(ri)
roleInstances.add(clusterId)(ris)
```

* Akka
  * Blocking actor
    * Let users call blocking methods, but rewrite it so they are all futures
      * think async macro

* Scalaz
  * for case class
    * auto build lens for case class
    * auto build equals
    * auto build show

* Coding Utils
  * make "case" in case class a macro
    * examples
        * `case class Foo(name: String)`
            * same as current day case class but update methods per field
        * `value class FooOpt(self: Foo)`
            * short-hand for `class FooOpt(val self: Foo) extends AnyVal`
        * `message class HttpIntent`
            * similar to case class, but without copy or any updating methods
    * This will be hard since this isn't supported in macros yet.  Could do it if each keyword is a 
        * annotation
            * `@case class Foo(name: String)`
            * `@value class FooOpt(self: Foo)`
            * `@message class HttpIntent`
        * trait
            * `class Foo(name: String) extends Case`
            * `class FooOpt(self: Foo) extends Value`
            * `class HttpIntent extends Message`
      
  * traits with vals and extending traits override, lower traits null
    * if you put a val with impl at trait A, and override it in trait B, all access of the val in trait A will see null
        * make this a compiler error if A accesses the val
  * reorder collections calls, and combine functions when possible (scala blits)
  * logging framework based off debug macro
  * if else chains not compiling if all states are not checked (sealed trait, adding new child)
  * given an object and a trait to mix in, create a proxy object with the trait mixed in
  * port the following from clojure
    * `(get-in users [:wont :exist :at :all] "couldn't find it!")`
    * `(update-in users [:david] (fn [user] (merge user {:win-factor 9001})))`
    * `(assoc-in users [:missing-user :age] 38)`
    * destructors for case classes, maps, and lists 
      * `def foo(name: Dest[String], age: Dest[Int]) = (name, age); foo(user("bob", 38)); foo(Map("name" -> "bob", "age" -> 38))`
        * maps are tricky, is there a HMap like HList?
        * Dest would have access to the function, but don't think it will have access to rewrite the caller...
