package scalacache.monix

import monix.eval.Task

import scalacache.{Async, Mode, Monad, SimpleMode}
import scalacache.cats.effect.CatsEffectInstances

object modes {

  implicit val task: Mode[Task, Task, Async] = new SimpleMode[Task, Async] {
    def S: Async[Task] = CatsEffectInstances.asyncForCatsEffectAsync[Task]
    def M: Monad[Task] = CatsEffectInstances.monadForCatsMonad[Task]
  }

}
