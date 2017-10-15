package scalacache.scalaz

import scalacache.{Async, Mode, Monad, SimpleMode}
import scalaz.concurrent.Task

object modes {

  implicit val task: Mode[Task, Task, Async] = new SimpleMode[Task, Async] {
    val S: Async[Task] = ScalazInstances.AsyncForScalazTask
    val M: Monad[Task] = ScalazInstances.MonadForScalazTask
  }

}
