package scalacache

import scala.concurrent.duration.Duration
import scala.language.experimental.macros
import scala.language.higherKinds

package object memoization {

  def memoize[F[_], V](cache: CacheAlg[V], ttl: Option[Duration])(f: => V)(implicit mode: Mode[F]): F[V] =
    macro Macros.memoize[F, V]

  def memoizeF[F[_], V](cache: CacheAlg[V], ttl: Option[Duration])(f: => F[V])(implicit mode: Mode[F]): F[V] =
    macro Macros.memoizeF[F, V]

}
