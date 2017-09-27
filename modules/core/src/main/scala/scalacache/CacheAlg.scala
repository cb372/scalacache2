package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

// TODO implicit flags

trait CacheAlg[V, M[F[_]] <: MonadErrorSync[F]] {

  def get[F[_]](keyParts: Any*)(implicit mode: Mode[F, M]): F[Option[V]]

  def put[F[_]](keyParts: Any*)(value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F, M]): F[Unit]

  def caching[F[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => V)(implicit mode: Mode[F, M]): F[V] =
    cachingF(keyParts: _*)(ttl)(mode.M.pure(f))

  def cachingF[F[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => F[V])(implicit mode: Mode[F, M]): F[V] = {
    import mode._
    M.flatMap(get(keyParts: _*)){
      case Some(valueFromCache) =>
        M.pure(valueFromCache)
      case None =>
        M.flatMap(f) { calculatedValue =>
          M.map(put(keyParts: _*)(calculatedValue, ttl))(_ => calculatedValue)
        }
    }
  }

}
