package scalacache.caffeine

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Mode, Sync}

class CaffeineCache[V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[V, Sync] {

  def getWithKey[F[_], G[_]](key: String)(implicit mode: Mode[F, G, Sync]): F[Option[V]] = {
    println(s"Get with key $key")
    // TODO logging
    mode.S.delay(Option.apply(underlying.getIfPresent(key)))
  }

  def putWithKey[F[_], G[_]](key: String, value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F, G, Sync]): F[Unit] =
    mode.S.delay(underlying.put(key, value))

}

// TODO factory methods in companion object
