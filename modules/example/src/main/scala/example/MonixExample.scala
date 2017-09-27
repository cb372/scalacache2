package example

import net.spy.memcached.{AddrUtil, BinaryConnectionFactory, MemcachedClient}

import scala.util.{Failure, Success}
import scalacache.CacheConfig
import scalacache.memcached.MemcachedCache

object MonixExample extends App {

  implicit val cacheConfig = CacheConfig()
  import scalacache.monix.modes.task

  // assume memcached is running on localhost
  val underlying = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"))
  val cache = new MemcachedCache[String](underlying)

  val putAndThenGet =
    for {
      _ <- cache.put("my-key")("hello monix!")
      value <- cache.get("my-key")
    } yield value.getOrElse("not found")

  import monix.execution.Scheduler.Implicits.global
  putAndThenGet.runAsync.onComplete {
    case Success(value) => println(s"Success! $value")
    case Failure(e) => println(s"Failure! ${e.getMessage}")
  }

}
