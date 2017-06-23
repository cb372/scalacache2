package scalacache

package object serialization {

  implicit val defaultCodecs = new BaseCodecs with JavaSerializationCodec

}
