package scalacache.cats.effect

import cats.effect.IO

import scalacache.{Mode, MonadErrorAsync}

object modes {

  implicit val io: Mode[IO, MonadErrorAsync] = new Mode[IO, MonadErrorAsync] {
    val M: MonadErrorAsync[IO] = CatsEffectInstances.monadErrorAsyncForCatsEffectAsync[IO]
  }

}
