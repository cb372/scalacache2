package scalacache

import scalacache.modes.sync.Id
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.higherKinds
import scala.util.control.NonFatal

// TODO the instances for Id and Future below are pretty dodgy monad-wise - rename to just Sync and Async?
trait MonadErrorSync[F[_]] {

  def pure[A](a: A): F[A]

  def delay[A](thunk: => A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def error[A](t: Throwable): F[A]

  def catchNonFatal[A](a: => A): F[A] =
    try pure(a) catch {
      case NonFatal(e) => error(e)
    }
}

trait MonadErrorAsync[F[_]] extends MonadErrorSync[F] {
  def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A]
}

object MonadErrorForId extends MonadErrorSync[Id] {

  def pure[A](a: A): Id[A] = a

  def delay[A](thunk: => A): Id[A] = thunk

  def map[A, B](fa: Id[A])(f: A => B): Id[B] = f(fa)

  def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

  def error[A](t: Throwable): Id[A] = throw t

}

class MonadErrorAsyncForFuture(implicit ec: ExecutionContext) extends MonadErrorAsync[Future] {

  def pure[A](a: A): Future[A] = Future.successful(a)

  // note: the Future will start immediately so this does not actually delay the side-effect
  def delay[A](thunk: => A): Future[A] = Future(thunk)

  def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)

  def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)

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
