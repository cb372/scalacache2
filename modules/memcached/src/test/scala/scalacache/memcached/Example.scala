package scalacache.memcached

import net.spy.memcached.{AddrUtil, BinaryConnectionFactory, MemcachedClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scalacache.CacheConfig
import scalacache.serialization.defaultCodecs._
import scalacache.modes.scalaFuture._

case class User(id: Int, name: String)

object Example extends App {

  implicit val cacheConfig = CacheConfig()

  val underlying = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"))
  val userCache = new MemcachedCache[User](underlying)

  println(Await.result(userCache.put[Future]("chris")(User(123, "Chris")), Duration.Inf))
  println(Await.result(userCache.get[Future]("chris"), Duration.Inf))

  underlying.shutdown()
}
