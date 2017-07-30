package scalacache.memcached

import net.spy.memcached.MemcachedClient

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig, Mode}
import scalacache.serialization.Codec

class MemcachedCache[V](
                                        client: MemcachedClient,
                                        keySanitizer: MemcachedKeySanitizer = ReplaceAndTruncateSanitizer())
                                      (implicit val config: CacheConfig, codec: Codec[V, Array[Byte]])
  extends AbstractCache[V]
  with MemcachedTTLConverter {

  def getWithKey[F[_]](key: String)(implicit mode: Mode[F]): F[Option[V]] = mode.point {
    val bytes = client.get(keySanitizer.toValidMemcachedKey(key))
    if (bytes == null)
      None
    else
      Some(codec.decode(bytes.asInstanceOf[Array[Byte]]))
  }

  def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration])(implicit mode: Mode[F]): F[Unit] = mode.point {
    val bytes = codec.encode(value)
    client.set(key, toMemcachedExpiry(ttl), bytes).get()
  }

}
