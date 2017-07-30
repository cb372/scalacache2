package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

// TODO implicit flags

trait CacheAlg[V] {

  def get[F[_]](keyParts: Any*)(implicit mode: Mode[F]): F[Option[V]]

  def put[F[_]](keyParts: Any*)(value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F]): F[Unit]

  import cats.syntax.functor._
  import cats.syntax.flatMap._

  def caching[F[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => V)(implicit mode: Mode[F]): F[V] =
    cachingF(keyParts: _*)(ttl)(mode.M.pure(f))

  def cachingF[F[_]](keyParts: Any*)(ttl: Option[Duration] = None)(f: => F[V])(implicit mode: Mode[F]): F[V] = {
    import mode._
    get(keyParts: _*).flatMap {
      case Some(valueFromCache) =>
        M.pure(valueFromCache)
      case None =>
        f.flatMap { calculatedValue =>
          put(keyParts: _*)(calculatedValue, ttl)
            .map(_ => calculatedValue)
        }
    }
  }

}
