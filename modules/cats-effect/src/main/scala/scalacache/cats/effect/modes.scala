package scalacache.cats.effect

import cats.effect.IO

import scalacache.{Async, Mode, Monad, SimpleMode}

object modes {

  implicit val io: Mode[IO, IO, Async] = new SimpleMode[IO, Async] {
    val S: Async[IO] = CatsEffectInstances.asyncForCatsEffectAsync[IO]
    val M: Monad[IO] = CatsEffectInstances.monadForCatsMonad[IO]
  }

}
