package scalacache.caffeine

import cats.Id
import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Modes}

abstract class CaffeineCache[F[_], V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[F, V] {

  def getWithKey(key: String): F[Option[V]] = {
    println(s"Get with key $key")
    point(Option.apply(underlying.getIfPresent(key)))
  }

  def putWithKey(key: String, value: V, ttl: Option[Duration] = None): F[Unit] =
    point(underlying.put(key, value))

}

case class SyncCaffeineCache[V <: Object](underlying: Cache[String, V])
                                         (override implicit val config: CacheConfig)
  extends CaffeineCache[Id, V](underlying)
  with Modes.Sync {

  override def cachingF(keyParts: Any*)(ttl: Option[Duration])(f: => Id[V]) =
    // delegate the "get or else put" logic to caffeine, as it's probably faster (TODO benchmark this)
    underlying.get(toKey(keyParts), _ => f)

}
