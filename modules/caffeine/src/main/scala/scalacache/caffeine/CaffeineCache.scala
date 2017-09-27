package scalacache.caffeine

import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Mode, MonadErrorSync}

class CaffeineCache[V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[V, MonadErrorSync] {

  def getWithKey[F[_]](key: String)(implicit mode: Mode[F, MonadErrorSync]): F[Option[V]] = {
    println(s"Get with key $key")
    // TODO logging
    mode.M.pure(Option.apply(underlying.getIfPresent(key)))
  }

  def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration] = None)(implicit mode: Mode[F, MonadErrorSync]): F[Unit] =
    mode.M.pure(underlying.put(key, value))

}

// TODO factory methods in companion object
