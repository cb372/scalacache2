package scalacache.memcached

import net.spy.memcached.{AddrUtil, BinaryConnectionFactory, MemcachedClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scalacache.Modes
import scalacache.serialization.defaultCodecs._

case class User(id: Int, name: String)

object Example extends App {

  val underlying = new MemcachedClient(new BinaryConnectionFactory(), AddrUtil.getAddresses("localhost:11211"))
  val userCache = new MemcachedCache[Future, User](underlying) with Modes.ScalaFuture { val ec = global }

  println(Await.result(userCache.put("chris", User(123, "Chris")), Duration.Inf))
  println(Await.result(userCache.get("chris"), Duration.Inf))

  underlying.shutdown()
}
