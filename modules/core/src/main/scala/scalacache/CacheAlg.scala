package scalacache

import cats.Monad

import scala.concurrent.duration.Duration
import scala.language.higherKinds

// TODO key should be a list of key parts
// TODO implicit flags

trait CacheAlg[F[_], V] {

  implicit def M: Monad[F]

  def point[A](a: => A): F[A]

  def get(keyParts: Any*): F[Option[V]]

  def put(keyParts: Any*)(value: V, ttl: Option[Duration] = None): F[Unit]

  import cats.syntax.functor._
  import cats.syntax.flatMap._

  def caching(keyParts: Any*)(ttl: Option[Duration] = None)(f: => V): F[V] =
    cachingF(keyParts: _*)(ttl)(M.pure(f))

  def cachingF(keyParts: Any*)(ttl: Option[Duration] = None)(f: => F[V]): F[V] = {
    get(keyParts: _*).flatMap {
      case Some(valueFromCache) =>
        // TODO logging?
        println(s"Cache hit for key $keyParts") // TODO can't turn the key parts into a key for logging - move method into AbstractCache?
        M.pure(valueFromCache)
      case None =>
        // TODO logging?
        println(s"Cache miss for key $keyParts, calculating value")
        f.flatMap { calculatedValue =>
          println("Calculated value")
          put(keyParts: _*)(calculatedValue, ttl)
            .map(_ => calculatedValue)
        }
    }
  }

}
