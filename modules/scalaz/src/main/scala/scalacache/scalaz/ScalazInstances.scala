package scalacache.scalaz

import scalacache.{Async, Monad}
import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.{Future, Task}

object ScalazInstances {

  val AsyncForScalazTask = new Async[Task] {

    override def async[A](register: (Either[Throwable, A] => Unit) => Unit) =
      new Task(Future.async(register).map(\/.fromEither))

    override def delay[A](thunk: => A) = Task.delay(thunk)

    override def error[A](t: Throwable) = Task.fail(t)

  }

  val MonadForScalazTask = new Monad[Task] {

    override def pure[A](a: A) = Task.now(a)

    override def map[A, B](fa: Task[A])(f: (A) => B) = fa.map(f)

    override def flatMap[A, B](fa: Task[A])(f: (A) => Task[B]) = fa.flatMap(f)

  }

}
