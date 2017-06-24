package scalacache

import scala.concurrent.duration.Duration

trait AbstractCache[F[_], V] extends CacheAlg[F, V]{

  def config: CacheConfig

  def getWithKey(key: String): F[Option[V]]

  def putWithKey(key: String, value: V, ttl: Option[Duration]): F[Unit]

  override def get(keyParts: Any*) = getWithKey(toKey(keyParts: _*))

  override def put(keyParts: Any*)(value: V, ttl: Option[Duration]) = putWithKey(toKey(keyParts: _*), value, ttl)

  protected def toKey(keyParts: Any*): String =
    config.cacheKeyBuilder.toCacheKey(keyParts)

}
