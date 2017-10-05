package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

trait AbstractCache[V, S[F[_]] <: Sync[F]] extends CacheAlg[V, S]{

  def config: CacheConfig

  protected def getWithKey[F[_], G[_]](key: String)(implicit mode: Mode[F, G, S]): F[Option[V]]

  protected def putWithKey[F[_], G[_]](key: String, value: V, ttl: Option[Duration])(implicit mode: Mode[F, G, S]): F[Unit]

  override def get[F[_], G[_]](keyParts: Any*)(implicit mode: Mode[F, G, S]): G[Option[V]] =
    mode.transform(getWithKey(toKey(keyParts: _*)))

  override def put[F[_], G[_]](keyParts: Any*)(value: V, ttl: Option[Duration])(implicit mode: Mode[F, G, S]): G[Unit] =
    mode.transform(putWithKey(toKey(keyParts: _*), value, ttl))

  protected def toKey(keyParts: Any*): String =
    config.cacheKeyBuilder.toCacheKey(keyParts)

}
