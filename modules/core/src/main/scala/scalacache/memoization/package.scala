package scalacache

import scala.concurrent.duration.Duration
import scala.language.experimental.macros
import scala.language.higherKinds

package object memoization {

  def memoize[F[_], G[_], S[X[_]] <: Sync[X], V](cache: CacheAlg[V, S], ttl: Option[Duration])(f: => V)(implicit mode: Mode[F, G, S]): G[V] =
    macro Macros.memoize[F, G, S, V]

  def memoizeF[F[_], G[_], S[X[_]] <: Sync[X], V](cache: CacheAlg[V, S], ttl: Option[Duration])(f: => F[V])(implicit mode: Mode[F, G, S]): G[V] =
    macro Macros.memoizeF[F, G, S, V]

}
