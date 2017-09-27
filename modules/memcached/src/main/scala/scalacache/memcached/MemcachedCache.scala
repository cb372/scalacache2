package scalacache.memcached

import java.io.IOException

import net.spy.memcached.MemcachedClient
import net.spy.memcached.internal.{GetCompletionListener, GetFuture, OperationCompletionListener, OperationFuture}

import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scala.util.control.NonFatal
import scalacache.{AbstractCache, CacheConfig, Mode, MonadErrorAsync}
import scalacache.serialization.Codec

class MemcachedCache[V](
                                        client: MemcachedClient,
                                        keySanitizer: MemcachedKeySanitizer = ReplaceAndTruncateSanitizer())
                                      (implicit val config: CacheConfig, codec: Codec[V, Array[Byte]])
  extends AbstractCache[V, MonadErrorAsync]
  with MemcachedTTLConverter {

  protected def getWithKey[F[_]](key: String)(implicit mode: Mode[F, MonadErrorAsync]): F[Option[V]] = {
    import mode._

    M.async { cb =>
      def success(value: Option[V]): Unit = cb(Right(value))
      def error(e: Throwable): Unit = cb(Left(e))

      val f = client.asyncGet(keySanitizer.toValidMemcachedKey(key))
      f.addListener(new GetCompletionListener {
        def onComplete(g: GetFuture[_]): Unit = {
          if (g.getStatus.isSuccess) {
            val bytes = g.get()
            if (bytes == null)
              success(None)
            else {
              try {
                success(Some(codec.decode(bytes.asInstanceOf[Array[Byte]])))
              } catch {
                case NonFatal(e) => error(e)
              }
            }
          } else {
            error(new IOException(g.getStatus.getMessage))
          }
        }
      })
    }
  }

  protected def putWithKey[F[_]](key: String, value: V, ttl: Option[Duration])(implicit mode: Mode[F, MonadErrorAsync]): F[Unit] = {
    import mode._

    M.async { cb =>
      def success(): Unit = cb(Right(()))
      def error(e: Throwable): Unit = cb(Left(e))

      val bytes = codec.encode(value)
      val f = client.set(keySanitizer.toValidMemcachedKey(key), toMemcachedExpiry(ttl), bytes)
      f.addListener(new OperationCompletionListener {
        def onComplete(g: OperationFuture[_]): Unit = {
          if (g.getStatus.isSuccess) {
            success()
          } else {
            error(new IOException(g.getStatus.getMessage))
          }
        }
      })
    }
  }

}
