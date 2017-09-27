package scalacache.monix

import monix.eval.Task

import scalacache.{Mode, MonadErrorAsync}
import scalacache.cats.effect.CatsEffectInstances

object modes {

  implicit val task: Mode[Task, MonadErrorAsync] = new Mode[Task, MonadErrorAsync] {
    override def M: MonadErrorAsync[Task] = CatsEffectInstances.monadErrorAsyncForCatsEffectAsync[Task]
  }

}
