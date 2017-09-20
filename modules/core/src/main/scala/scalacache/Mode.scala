package scalacache

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait Mode[F[_], +M[G[_]] <: MonadError[G]] {

  def M: M[F]

}

object modes {

  object sync {

    type Id[X] = X

    implicit val mode: Mode[Id, MonadError] = new Mode[Id, MonadError] {
      val M: MonadError[Id] = MonadErrorForId
    }
  }

  object scalaFuture {
    implicit def mode(implicit executionContext: ExecutionContext): Mode[Future, MonadErrorAsync] =
      new Mode[Future, MonadErrorAsync] {
        val M: MonadErrorAsync[Future] = new MonadErrorAsyncForFuture
      }
  }

}

