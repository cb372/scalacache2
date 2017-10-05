package scalacache

import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scalacache.caffeine.CaffeineCache

case class User(id: Int, name: String)

object Example extends App {

  implicit val cacheConfig = CacheConfig()

  {
    import scalacache.modes.sync._
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[User](underlying)

    println(s"Get: ${userCache.get("chris")}")
    println(s"Put: ${userCache.put("chris")(User(123, "Chris"))}")
    println(s"Get: ${userCache.get("chris")}")

    println(s"Caching: ${userCache.caching("dave")(){ User(456, "Dave") }}")
    println(s"CachingF: ${userCache.caching("bob")(){ User(789, "Bob") }}")
  }

  println("---")

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scalacache.modes.scalaFuture._

    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[User](underlying)

    val get1 = userCache.get("chris")
    println(s"Get: ${Await.result(get1, Duration.Inf)}")

    val put1 = userCache.put("chris")(User(123, "Chris"))
    println(s"Put: ${Await.result(put1, Duration.Inf)}")

    val get2 = userCache.get("chris")
    println(s"Get: ${Await.result(get2, Duration.Inf)}")

    val dave = userCache.caching("dave")(){ User(456, "Dave") }
    println(s"Caching: ${Await.result(dave, Duration.Inf)}")

    val bob = userCache.cachingF("bob")(){ Future { Thread.sleep(1000); User(789, "Bob") } }
    println(s"CachingF: ${Await.result(bob, Duration.Inf)}")
  }

  println("---")

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    import scalacache.modes.scalaFuture._
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[User](underlying)

    import scalacache.memoization._
    def getUser(id: Int): Future[User] = memoize(userCache, None){ User(id, "the user")}
    def getUserF(id: Int): Future[User] = memoizeF(userCache, None){ Future { Thread.sleep(1000); User(id, "the user") } }
    println(s"Memoize: ${Await.result(getUser(111), Duration.Inf)}")
    println(s"MemoizeF: ${Await.result(getUserF(999), Duration.Inf)}")
  }

}
