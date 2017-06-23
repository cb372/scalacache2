package scalacache.serialization

import scala.annotation.implicitNotFound
import scala.language.implicitConversions

@implicitNotFound("Could not find any Codec for type ${From} and ${Repr}")
trait Codec[From, Repr] {
  def encode(value: From): Repr
  def decode(data: Repr): From
}

/**
 * For simple primitives, we provide lightweight Codecs for ease of use.
 */
object Codec extends BaseCodecs