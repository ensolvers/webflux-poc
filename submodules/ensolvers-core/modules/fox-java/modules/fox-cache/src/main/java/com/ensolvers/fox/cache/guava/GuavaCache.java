package com.ensolvers.fox.cache.guava;

import com.ensolvers.fox.cache.common.GenericCache;
import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * G
 *
 * @param <T>
 */
public class GuavaCache<T> implements GenericCache<T> {

  private final LoadingCache<String, T> cache;
  private final String keyPrefix;

  public GuavaCache(Function<String, T> fetchingFunction, String keyPrefix, int expirationTimeInSeconds) {
    this.keyPrefix = keyPrefix;
    this.cache = CacheBuilder.newBuilder().expireAfterAccess(expirationTimeInSeconds, TimeUnit.SECONDS).build(new CacheLoader<>() {
      @Override
      public T load(String key) {
        return fetchingFunction.apply(key);
      }
    });
  }

  @Override
  public T get(String key) {
    try {
      return this.cache.get(key);
    } catch (ExecutionException e) {
      throw new CacheExecutionException("Error when trying to get an item from the cache", e);
    }
  }

  @Override
  public void invalidate(String key) {
    this.cache.refresh(key);
  }

  @Override
  public void put(String key, T object) {
    this.cache.put(key, object);
  }
}
