package scalacache

import scala.concurrent.duration.Duration
import scala.language.experimental.macros
import scala.language.higherKinds

package object memoization {

  def memoize[F[_], M[G[_]] <: MonadErrorSync[G], V](cache: CacheAlg[V, M], ttl: Option[Duration])(f: => V)(implicit mode: Mode[F, M]): F[V] =
    macro Macros.memoize[F, M, V]

  def memoizeF[F[_], M[G[_]] <: MonadErrorSync[G], V](cache: CacheAlg[V, M], ttl: Option[Duration])(f: => F[V])(implicit mode: Mode[F, M]): F[V] =
    macro Macros.memoizeF[F, M, V]

}
