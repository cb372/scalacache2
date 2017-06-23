package scalacache

import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scalacache.caffeine.{CaffeineCache, SyncCaffeineCache}

case class User(id: Int, name: String)

object Example extends App {

  {
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new SyncCaffeineCache[User](underlying)

    println(s"Get: ${userCache.get("chris")}")
    println(s"Put: ${userCache.put("chris", User(123, "Chris"))}")
    println(s"Get: ${userCache.get("chris")}")
  }

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[Future, User](underlying) with Modes.ScalaFuture { val ec = global }

    println(s"Get: ${Await.result(userCache.get("chris"), Duration.Inf)}")
    println(s"Put: ${Await.result(userCache.put("chris", User(123, "Chris")), Duration.Inf)}")
    println(s"Get: ${Await.result(userCache.get("chris"), Duration.Inf)}")

  }

}
