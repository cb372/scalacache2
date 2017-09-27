package scalacache

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Mode[F[_], +M[G[_]] <: MonadErrorSync[G]] {

  def M: M[F]

}

object modes {

  object sync {

    type Id[X] = X

    implicit val mode: Mode[Id, MonadErrorSync] = new Mode[Id, MonadErrorSync] {
      val M: MonadErrorSync[Id] = MonadErrorForId
    }
  }

  object scalaFuture {
    implicit def mode(implicit executionContext: ExecutionContext): Mode[Future, MonadErrorAsync] =
      new Mode[Future, MonadErrorAsync] {
        val M: MonadErrorAsync[Future] = new MonadErrorAsyncForFuture
      }
  }

}

