package scalacache

import cats.{Id, Monad}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Mode[F[_]] {

  implicit def M: Monad[F]

  def point[A](a: => A): F[A]

}

object modes {

  object sync {
    implicit val mode: Mode[Id] = new Mode[Id] {
      implicit def M: Monad[Id] = cats.catsInstancesForId
      def point[A](a: => A): Id[A] = a
    }
  }

  object scalaFuture {
    implicit def mode(implicit executionContext: ExecutionContext): Mode[Future] = new Mode[Future] {
      lazy val M: Monad[Future] = cats.instances.future.catsStdInstancesForFuture(executionContext)

      def point[A](a: => A): Future[A] = Future(a)(executionContext)
    }
  }

}

