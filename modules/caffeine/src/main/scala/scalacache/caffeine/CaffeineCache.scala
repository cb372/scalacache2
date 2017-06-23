package scalacache.caffeine

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.CacheAlg

abstract class CaffeineCache[F[_], V <: Object](underlying: Cache[String, Object]) extends CacheAlg[F, V] {

  def get(key: String): F[Option[V]] =
    point(Option.apply(underlying.getIfPresent(key).asInstanceOf[V]))

  def put(key: String, value: V, ttl: Option[Duration] = None): F[Unit] =
    point(underlying.put(key, value))

}
