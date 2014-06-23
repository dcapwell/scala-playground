* Convert case class to: tuple, list, map
* Convert tuple/map to case class
* macro type that auto adds FunctionX based off apply (extends Fn)
* add "with" methods for each field
* tuple mutation methods: x :: tuple(a, b) => tuple(x, a, b)


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
  * traits with vals and extending traits override, lower traits null
  * reorder collections calls, and combine functions when possible
  * logging framework based off debug macro
  * if else chains not compiling if all states are not checked (sealed trait, adding new child)
  * given an object and a trait to mix in, create a proxy object with the trait mixed in
  * port the following from clojure
    * `(get-in users [:wont :exist :at :all] "couldn't find it!")`
    * `(update-in users [:david] (fn [user] (merge user {:win-factor 9001})))`
    * `(assoc-in users [:missing-user :age] 38)`
    * destructors for case classes, maps, and lists 
      * `def foo(input: Destructor("name", "age")) = (input.name, input.age); foo(User("bob", 38))`
