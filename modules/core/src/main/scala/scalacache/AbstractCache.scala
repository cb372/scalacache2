package scalacache

trait AbstractCache[F[_], V] extends CacheAlg[F, V]{

  def config: CacheConfig

  // TODO wrappers for get and put that take care of building the cache key

}
