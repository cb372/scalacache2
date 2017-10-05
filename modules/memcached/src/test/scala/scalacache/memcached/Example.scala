package scalacache.memcached

import net.spy.memcached.{AddrUtil, BinaryConnectionFactory, MemcachedClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scalacache.CacheConfig
import scalacache.serialization.defaultCodecs._

case class User(id: Int, name: String)

object Example extends App {

  implicit val cacheConfig = CacheConfig()
  import scalacache.modes.scalaFuture._

  val underlying = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"))
  val userCache = new MemcachedCache[User](underlying)

  val put = userCache.put("chris")(User(123, "Chris"))
  println(Await.result(put, Duration.Inf))

  val get = userCache.get("chris")
  println(Await.result(get, Duration.Inf))

  underlying.shutdown()
}
