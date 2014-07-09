package object macros {
  /**
   * Case Function is just short-hand for PartialFunction
   */
  type =>?[-A, +B] = PartialFunction[A, B]
}
