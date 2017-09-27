package scalacache.cats.effect

import cats.effect.{Async, IO}

import scala.language.higherKinds
import scalacache.MonadErrorAsync

object CatsEffectInstances {

  def monadErrorAsyncForCatsEffectAsync[F[_]](implicit af: Async[F]): MonadErrorAsync[F] = new MonadErrorAsync[F] {

    def pure[A](a: A): F[A] = af.pure(a)

    def delay[A](thunk: => A): F[A] = af.delay(thunk)

    def map[A, B](fa: F[A])(f: A => B): F[B] = af.map(fa)(f)

    def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B] = af.flatMap(fa)(f)

    def error[A](t: Throwable): F[A] = af.raiseError(t)

    def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A] = af.async(register)

  }

}
