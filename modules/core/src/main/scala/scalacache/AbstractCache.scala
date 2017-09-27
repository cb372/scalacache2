package scalacache

import scala.concurrent.duration.Duration

import scala.language.higherKinds

trait AbstractCache[V, M[F[_]] <: MonadErrorSync[F]] extends CacheAlg[V, M]{

  def config: CacheConfig

  protected def getWithKey[F[_]](key: String)(implicit mode: Mode[F, M]): F[Option[V]]

  protected def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration])(implicit mode: Mode[F, M]): F[Unit]

  override def get[F[_]](keyParts: Any*)(implicit mode: Mode[F, M]): F[Option[V]] =
    getWithKey(toKey(keyParts: _*))

  override def put[F[_]](keyParts: Any*)(value: V, ttl: Option[Duration])(implicit mode: Mode[F, M]): F[Unit] =
    putWithKey(toKey(keyParts: _*), value, ttl)

  protected def toKey(keyParts: Any*): String =
    config.cacheKeyBuilder.toCacheKey(keyParts)

}
