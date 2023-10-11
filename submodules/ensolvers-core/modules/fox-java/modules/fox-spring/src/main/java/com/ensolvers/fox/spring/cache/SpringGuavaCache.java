package com.ensolvers.fox.spring.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Spring Cache compatible implementation using Guava as ahe underlying in-memory cache
 * 
 * @param <CacheType> the type of objects that are going to be cached
 */
public class SpringGuavaCache<CacheType> extends AbstractValueAdaptingCache {

  private static Logger logger = LoggerFactory.getLogger(SpringGuavaCache.class);

  private String name;
  private Cache<String, CacheType> cache;

  public SpringGuavaCache(boolean allowNullValues, String name) {
    this(allowNullValues, name, CacheBuilder.newBuilder().build());
  }

  public SpringGuavaCache(boolean allowNullValues, String name, long expirationTime, TimeUnit expirationTimeUnit) {
    this(allowNullValues, name, CacheBuilder.newBuilder().expireAfterAccess(expirationTime, expirationTimeUnit).build());
  }

  public SpringGuavaCache(boolean allowNullValues, String name, Cache<String, CacheType> cache) {
    super(allowNullValues);

    this.name = name;
    this.cache = cache;
  }

  @Override
  protected Object lookup(Object key) {
    return this.cache.asMap().get(key.toString());
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Object getNativeCache() {
    return this.cache;
  }

  @Override
  public <T> T get(Object key, Callable<T> valueLoader) {
    try {
      return (T) this.cache.get(key.toString(), () -> (CacheType) valueLoader.call());
    } catch (ExecutionException e) {
      logger.error("Error when trying to fetch object from cache, key = " + key.toString(), e);
    }
    return null;
  }

  @Override
  public void put(Object key, Object value) {
    if (value == null) {
      return;
    }

    this.cache.put(key.toString(), (CacheType) value);
  }

  @Override
  public void evict(Object key) {
    this.cache.invalidate(key.toString());
  }

  @Override
  public void clear() {
    this.cache.invalidateAll();
  }
}
