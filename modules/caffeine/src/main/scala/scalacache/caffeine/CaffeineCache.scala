package scalacache.caffeine

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Mode, MonadError}

class CaffeineCache[V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[V, MonadError] {

  def getWithKey[F[_]](key: String)(implicit mode: Mode[F, MonadError]): F[Option[V]] = {
    println(s"Get with key $key")
    // TODO logging
    mode.M.pure(Option.apply(underlying.getIfPresent(key)))
  }

  def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F, MonadError]): F[Unit] =
    mode.M.pure(underlying.put(key, value))

}

// TODO factory methods in companion object
