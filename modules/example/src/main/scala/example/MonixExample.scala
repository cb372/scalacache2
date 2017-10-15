package example

import net.spy.memcached.{AddrUtil, BinaryConnectionFactory, MemcachedClient}

import scala.util.{Failure, Success}
import scalacache.CacheConfig
import scalacache.memcached.MemcachedCache

object MonixExample extends App {

  implicit val cacheConfig = CacheConfig()

  // assume memcached is running on localhost
  val underlying = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"))
  val cache = new MemcachedCache[String](underlying)

  {
    import scalacache.monix.modes.task

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

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scalacache.modes.sync._

    cache.put("my-sync-key")("hello sync monix!")
    println(cache.get("my-sync-key").getOrElse("not found"))
  }

  {
    import scalacache.scalaz.modes.task

    val putAndThenGet =
      for {
        _ <- cache.put("my-scalaz-key")("hello scalaz!")
        value <- cache.get("my-scalaz-key")
      } yield value.getOrElse("not found")

    println(putAndThenGet.unsafePerformSync)
  }

  underlying.shutdown()

}
