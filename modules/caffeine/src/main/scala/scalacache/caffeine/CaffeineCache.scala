package scalacache.caffeine

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Mode}

class CaffeineCache[V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[V] {

  def getWithKey[F[_]](key: String)(implicit mode: Mode[F]): F[Option[V]] = {
    println(s"Get with key $key")
    // TODO logging
    mode.point(Option.apply(underlying.getIfPresent(key)))
  }

  def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F]): F[Unit] =
    mode.point(underlying.put(key, value))

}

// TODO factory methods in companion object
