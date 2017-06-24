package scalacache.caffeine

import cats.Id
import com.github.benmanes.caffeine.cache.Cache

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheAlg, CacheConfig, Modes}

abstract class CaffeineCache[F[_], V <: Object](underlying: Cache[String, V])
                                               (implicit val config: CacheConfig)
  extends AbstractCache[F, V] {

  def get(key: String): F[Option[V]] =
    point(Option.apply(underlying.getIfPresent(key)))

  def put(key: String, value: V, ttl: Option[Duration] = None): F[Unit] =
    point(underlying.put(key, value))

}

case class SyncCaffeineCache[V <: Object](underlying: Cache[String, V])
                                         (override implicit val config: CacheConfig)
  extends CaffeineCache[Id, V](underlying)
  with Modes.Sync {

  override def cachingF(key: String)(ttl: Option[Duration])(f: => Id[V]) =
    // delegate the "get or else put" logic to caffeine, as it's probably faster (TODO benchmark this)
    underlying.get(key, _ => f)

}
