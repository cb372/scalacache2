package scalacache

import cats.{Id, Monad}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

object Modes {

  trait Sync {
    implicit def M: Monad[Id] = cats.catsInstancesForId
    def point[A](a: => A): Id[A] = a
  }

  trait Futures {
    val ec: ExecutionContext
    lazy val M: Monad[Future] = cats.instances.future.catsStdInstancesForFuture(ec)
    def point[A](a: => A): Future[A] = Future(a)(ec)
  }

}

