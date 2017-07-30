package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

trait AbstractCache[V] extends CacheAlg[V]{

  def config: CacheConfig

  def getWithKey[F[_]](key: String)(implicit mode: Mode[F]): F[Option[V]]

  def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration])(implicit mode: Mode[F]): F[Unit]

  override def get[F[_]](keyParts: Any*)(implicit mode: Mode[F]): F[Option[V]] =
    getWithKey(toKey(keyParts: _*))

  override def put[F[_]](keyParts: Any*)(value: V, ttl: Option[Duration])(implicit mode: Mode[F]): F[Unit] =
    putWithKey(toKey(keyParts: _*), value, ttl)

  protected def toKey(keyParts: Any*): String =
    config.cacheKeyBuilder.toCacheKey(keyParts)

}
