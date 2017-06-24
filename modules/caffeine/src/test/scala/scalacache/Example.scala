package scalacache

import com.github.benmanes.caffeine.cache.Caffeine

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scalacache.caffeine.{CaffeineCache, SyncCaffeineCache}

case class User(id: Int, name: String)

object Example extends App {

  implicit val cacheConfig = CacheConfig()

  {
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new SyncCaffeineCache[User](underlying)

    println(s"Get: ${userCache.get("chris")}")
    println(s"Put: ${userCache.put("chris")(User(123, "Chris"))}")
    println(s"Get: ${userCache.get("chris")}")

    println(s"Caching: ${userCache.caching("dave")(){ User(456, "Dave") }}")
    println(s"CachingF: ${userCache.caching("bob")(){ User(789, "Bob") }}")
  }

  println("---")

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[Future, User](underlying) with Modes.ScalaFuture { val ec = global }

    println(s"Get: ${Await.result(userCache.get("chris"), Duration.Inf)}")
    println(s"Put: ${Await.result(userCache.put("chris")(User(123, "Chris")), Duration.Inf)}")
    println(s"Get: ${Await.result(userCache.get("chris"), Duration.Inf)}")

    println(s"Caching: ${Await.result(userCache.caching("dave")(){ User(456, "Dave") }, Duration.Inf)}")
    println(s"Caching: ${Await.result(userCache.cachingF("bob")(){ Future { Thread.sleep(1000); User(789, "Bob") } }, Duration.Inf)}")
  }

  println("---")

  {
    import scala.concurrent.ExecutionContext.Implicits.global
    val underlying = Caffeine.newBuilder().build[String, User]()
    val userCache = new CaffeineCache[Future, User](underlying) with Modes.ScalaFuture { val ec = global }

    import scalacache.memoization._
    def getUser(id: Int): Future[User] = memoize(userCache, None){ User(id, "the user")}
    def getUserF(id: Int): Future[User] = memoizeF(userCache, None){ Future { Thread.sleep(1000); User(id, "the user") } }
    println(s"Memoize: ${Await.result(getUser(111), Duration.Inf)}")
    println(s"MemoizeF: ${Await.result(getUserF(999), Duration.Inf)}")
  }

}
