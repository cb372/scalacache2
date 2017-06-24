package scalacache

import scala.concurrent.duration.Duration
import scala.language.experimental.macros
import scala.language.higherKinds

package object memoization {

  def memoize[F[_], V](cache: CacheAlg[F, V], ttl: Option[Duration])(f: => V): F[V] =
    macro Macros.memoize[F, V]

  def memoizeF[F[_], V](cache: CacheAlg[F, V], ttl: Option[Duration])(f: => F[V]): F[V] =
    macro Macros.memoizeF[F, V]

}
