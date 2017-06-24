package scalacache.memcached

import net.spy.memcached.MemcachedClient

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scalacache.{AbstractCache, CacheConfig}
import scalacache.serialization.Codec

abstract class MemcachedCache[F[_], V](
                                        client: MemcachedClient,
                                        keySanitizer: MemcachedKeySanitizer = ReplaceAndTruncateSanitizer())
                                      (implicit val config: CacheConfig, codec: Codec[V, Array[Byte]])
  extends AbstractCache[F, V]
  with MemcachedTTLConverter {

  def get(key: String) = point {
    val bytes = client.get(keySanitizer.toValidMemcachedKey(key))
    if (bytes == null)
      None
    else
      Some(codec.decode(bytes.asInstanceOf[Array[Byte]]))
  }

  def put(key: String, value: V, ttl: Option[Duration]) = point {
    val bytes = codec.encode(value)
    client.set(key, toMemcachedExpiry(ttl), bytes).get()
  }

}
