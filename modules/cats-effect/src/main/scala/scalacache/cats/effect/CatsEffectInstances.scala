package scalacache.cats.effect

import cats.{Monad => CatsMonad}
import cats.effect.{IO, Async => CatsAsync}

import scala.language.higherKinds
import scalacache.{Async, Monad}

object CatsEffectInstances {

  def asyncForCatsEffectAsync[F[_]](implicit af: CatsAsync[F]): Async[F] = new Async[F] {

    def delay[A](thunk: => A): F[A] = af.delay(thunk)

    def error[A](t: Throwable): F[A] = af.raiseError(t)

    def async[A](register: (Either[Throwable, A] => Unit) => Unit): F[A] = af.async(register)

  }

  def monadForCatsMonad[F[_]](implicit M: CatsMonad[F]): Monad[F] = new Monad[F] {

    override def pure[A](a: A): F[A] = M.pure(a)

    override def flatMap[A, B](fa: F[A])(f: (A) => F[B]): F[B] = M.flatMap(fa)(f)

    override def map[A, B](fa: F[A])(f: (A) => B): F[B] = M.map(fa)(f)
  }

}
