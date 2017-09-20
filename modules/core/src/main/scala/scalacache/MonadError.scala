package scalacache

import cats.Id

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.higherKinds
import scala.util.control.NonFatal

trait MonadError[F[_]] {

  def pure[A](a: A): F[A]

  def map[A, B](fa: F[A])(f: A => B): F[B]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]

  def error[A](t: Throwable): F[A]

  def catchNonFatal[A](a: => A): F[A] =
    try pure(a) catch {
      case NonFatal(e) => error(e)
    }
}

trait MonadErrorAsync[F[_]] extends MonadError[F] {
  def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A]
}

object MonadErrorForId extends MonadError[Id] {

  def pure[A](a: A): Id[A] = a

  def map[A, B](fa: Id[A])(f: A => B): Id[B] = f(fa)

  def flatMap[A, B](fa: Id[A])(f: A => Id[B]): Id[B] = f(fa)

  def error[A](t: Throwable): Id[A] = throw t

}

class MonadErrorAsyncForFuture(implicit ec: ExecutionContext) extends MonadErrorAsync[Future] {

  def pure[A](a: A): Future[A] = Future.successful(a)

  def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)

  def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)

  def error[A](t: Throwable): Future[A] = Future.failed(t)

  def async[A](register: ((Either[Throwable, A]) => Unit) => Unit): Future[A] = {
    val promise = Promise[A]()
    register {
      case Left(t)  => promise.failure(t)
      case Right(t) => promise.success(t)
    }
    promise.future
  }

}
