package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

// TODO implicit flags

trait CacheAlg[V, S[F[_]] <: Sync[F]] {

  def get[F[_], G[_]](keyParts: Any*)(implicit mode: Mode[F, G, S]): G[Option[V]]

  def put[F[_], G[_]](keyParts: Any*)(value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F, G, S]): G[Unit]

  def caching[F[_], G[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => V)(implicit mode: Mode[F, G, S]): G[V] =
    cachingF(keyParts: _*)(ttl)(mode.S.delay(f))

  def cachingF[F[_], G[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => F[V])(implicit mode: Mode[F, G, S]): G[V] = {
    import mode._
    M.flatMap(get(keyParts: _*)){
      case Some(valueFromCache) =>
        M.pure(valueFromCache)
      case None =>
        M.flatMap(transform(f)) { calculatedValue =>
          M.map(put(keyParts: _*)(calculatedValue, ttl))(_ => calculatedValue)
        }
    }
  }

}
