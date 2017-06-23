package scalacache.serialization

/**
 * Primitive type Codec instances
 *
 * Credit: Shade @ https://github.com/alexandru/shade/blob/master/src/main/scala/shade/memcached/Codec.scala
 */
trait BaseCodecs {

  implicit object IntBinaryCodec extends Codec[Int, Array[Byte]] {
    def encode(value: Int): Array[Byte] =
      Array(
        (value >>> 24).asInstanceOf[Byte],
        (value >>> 16).asInstanceOf[Byte],
        (value >>> 8).asInstanceOf[Byte],
        value.asInstanceOf[Byte]
      )

    def decode(data: Array[Byte]): Int =
      (data(0).asInstanceOf[Int] & 255) << 24 |
        (data(1).asInstanceOf[Int] & 255) << 16 |
        (data(2).asInstanceOf[Int] & 255) << 8 |
        data(3).asInstanceOf[Int] & 255
  }

  implicit object DoubleBinaryCodec extends Codec[Double, Array[Byte]] {
    import java.lang.{ Double => JvmDouble }
    def encode(value: Double): Array[Byte] = {
      val l = JvmDouble.doubleToLongBits(value)
      LongBinaryCodec.encode(l)
    }

    def decode(data: Array[Byte]): Double = {
      val l = LongBinaryCodec.decode(data)
      JvmDouble.longBitsToDouble(l)
    }
  }

  implicit object FloatBinaryCodec extends Codec[Float, Array[Byte]] {
    import java.lang.{ Float => JvmFloat }
    def encode(value: Float): Array[Byte] = {
      val i = JvmFloat.floatToIntBits(value)
      IntBinaryCodec.encode(i)
    }

    def decode(data: Array[Byte]): Float = {
      val i = IntBinaryCodec.decode(data)
      JvmFloat.intBitsToFloat(i)
    }
  }

  implicit object LongBinaryCodec extends Codec[Long, Array[Byte]] {
    def encode(value: Long): Array[Byte] =
      Array(
        (value >>> 56).asInstanceOf[Byte],
        (value >>> 48).asInstanceOf[Byte],
        (value >>> 40).asInstanceOf[Byte],
        (value >>> 32).asInstanceOf[Byte],
        (value >>> 24).asInstanceOf[Byte],
        (value >>> 16).asInstanceOf[Byte],
        (value >>> 8).asInstanceOf[Byte],
        value.asInstanceOf[Byte]
      )

    def decode(data: Array[Byte]): Long =
      (data(0).asInstanceOf[Long] & 255) << 56 |
        (data(1).asInstanceOf[Long] & 255) << 48 |
        (data(2).asInstanceOf[Long] & 255) << 40 |
        (data(3).asInstanceOf[Long] & 255) << 32 |
        (data(4).asInstanceOf[Long] & 255) << 24 |
        (data(5).asInstanceOf[Long] & 255) << 16 |
        (data(6).asInstanceOf[Long] & 255) << 8 |
        data(7).asInstanceOf[Long] & 255
  }

  implicit object BooleanBinaryCodec extends Codec[Boolean, Array[Byte]] {
    def encode(value: Boolean): Array[Byte] =
      Array((if (value) 1 else 0).asInstanceOf[Byte])

    def decode(data: Array[Byte]): Boolean =
      data.isDefinedAt(0) && data(0) == 1
  }

  implicit object CharBinaryCodec extends Codec[Char, Array[Byte]] {
    def encode(value: Char): Array[Byte] = Array(
      (value >>> 8).asInstanceOf[Byte],
      value.asInstanceOf[Byte]
    )

    def decode(data: Array[Byte]): Char =
      ((data(0).asInstanceOf[Int] & 255) << 8 |
        data(1).asInstanceOf[Int] & 255)
        .asInstanceOf[Char]
  }

  implicit object ShortBinaryCodec extends Codec[Short, Array[Byte]] {
    def encode(value: Short): Array[Byte] = Array(
      (value >>> 8).asInstanceOf[Byte],
      value.asInstanceOf[Byte]
    )

    def decode(data: Array[Byte]): Short =
      ((data(0).asInstanceOf[Short] & 255) << 8 |
        data(1).asInstanceOf[Short] & 255)
        .asInstanceOf[Short]
  }

  implicit object StringBinaryCodec extends Codec[String, Array[Byte]] {
    def encode(value: String): Array[Byte] = value.getBytes("UTF-8")
    def decode(data: Array[Byte]): String = new String(data, "UTF-8")
  }

  implicit object ArrayByteBinaryCodec extends Codec[Array[Byte], Array[Byte]] {
    def encode(value: Array[Byte]): Array[Byte] = value
    def decode(data: Array[Byte]): Array[Byte] = data
  }
}
