package scalacache

import scalacache.modes.sync.Id
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.higherKinds
import scala.util.control.NonFatal

trait Sync[F[_]] {

  def delay[A](thunk: => A): F[A]

  def error[A](t: Throwable): F[A]
}

trait Async[F[_]] extends Sync[F] {
  def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A]
}

object SyncForId extends Sync[Id] {

  def delay[A](thunk: => A): Id[A] = thunk

  def error[A](t: Throwable): Id[A] = throw t

}

class AsyncForFuture(implicit ec: ExecutionContext) extends Async[Future] {

  // note: the Future will start immediately so this does not actually delay the side-effect
  def delay[A](thunk: => A): Future[A] = Future(thunk)

  def error[A](t: Throwable): Future[A] = Future.failed(t)

  def async[A](register: (Either[Throwable, A] => Unit) => Unit): Future[A] = {
    val promise = Promise[A]()
    register {
      case Left(t)  => promise.failure(t)
      case Right(t) => promise.success(t)
    }
    promise.future
  }

}
