package scalacache

import cats.Monad

import scala.concurrent.duration.Duration
import scala.language.higherKinds

// TODO key should be a list of key parts
// TODO implicit flags
// TODO other config
// TODO memoization

trait CacheAlg[F[_], V] {

  implicit def M: Monad[F]

  def point[A](a: => A): F[A]

  def get(key: String): F[Option[V]]

  def put(key: String, value: V, ttl: Option[Duration] = None): F[Unit]

  import cats.syntax.functor._
  import cats.syntax.flatMap._

  def caching(key: String)(ttl: Option[Duration] = None)(f: => V): F[V] = {
    get(key).flatMap {
      case Some(valueFromCache) =>
        // TODO logging?
        M.pure(valueFromCache)
      case None =>
        // TODO logging?
        val calculatedValue = f
        put(key, calculatedValue, ttl)
          .map(_ => calculatedValue)
    }
  }

}
