package scalacache

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.higherKinds

trait Mode[F[_], G[_], +S[X[_]] <: Sync[X]] {

  def S: S[F]

  def M: Monad[G]

  def transform[A](fa: F[A]): G[A]

}

trait SimpleMode[F[_], +S[X[_]] <: Sync[X]] extends Mode[F, F, S] {

  def transform[A](fa: F[A]): F[A] = fa

}

object modes {

  object sync {

    type Id[X] = X

    implicit val mode: Mode[Id, Id, Sync] = new SimpleMode[Id, Sync] {
      val M: Monad[Id] = MonadForId
      val S: Sync[Id] = SyncForId
    }

    implicit def await(implicit executionContext: ExecutionContext): Mode[Future, Id, Async] =
      new Mode[Future, Id, Async] {
        val S: Async[Future] = new AsyncForFuture
        val M: Monad[Id] = MonadForId
        def transform[A](fa: Future[A]): Id[A] = Await.result(fa, Duration.Inf)
      }
  }

  object scalaFuture {
    implicit def mode(implicit executionContext: ExecutionContext): Mode[Future, Future, Async] =
      new SimpleMode[Future, Async] {
        val M: Monad[Future] = new MonadForFuture
        val S: Async[Future] = new AsyncForFuture
      }
  }

}
