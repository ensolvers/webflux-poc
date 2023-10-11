package com.ensolvers.fox.cache.guava;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GuavaLoadingCache<V> extends AbstractSimpleLoadingCache<V> {

  private final LoadingCache<String, V> cache;

  private static Logger logger = LoggerFactory.getLogger(GuavaLoadingCache.class);

  public GuavaLoadingCache(int duration, String name, Function<String, V> loader) {
    super();
    this.cache = CacheBuilder.newBuilder().expireAfterWrite(duration, TimeUnit.SECONDS).build(new CacheLoader<>() {
      @Override
      public V load(String key) throws Exception {
        logger.info("Start fetching key [{}] for cache [{}]", key, name);
        V object = loader.apply(key);
        logger.info("Ended fetching key [{}] for cache [{}]", key, name);

        return object;
      }
    });
  }

  @Override
  public V getUnchecked(String key) {
    return this.cache.getUnchecked(key);
  }

  @Override
  public void invalidateEntry(String key) {
    this.cache.invalidate(key);
  }

}
