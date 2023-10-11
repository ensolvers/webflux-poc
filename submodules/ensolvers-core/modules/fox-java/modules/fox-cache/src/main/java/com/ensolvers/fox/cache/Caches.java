package com.ensolvers.fox.cache;

import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import com.ensolvers.fox.cache.guava.GuavaLoadingCache;
import com.ensolvers.fox.cache.redis.v3.RedisAsyncLoadingCache;
import com.ensolvers.fox.cache.redis.v3.RedisLoadingCache;
import com.ensolvers.fox.services.util.JSONSerializer;
import com.fasterxml.jackson.databind.JavaType;

import java.util.function.Function;

public class Caches {

  /**
   * Creates new Guava loading (in-memory) cache with the following params
   * @param duration duration of the entries
   * @param name name of the cache - to be identified in logs
   * @param loader function that loads in object that is not present in the cahce
   * @return the instantiated cache
   * @param <V> type of the entries stored in the cache
   */
  public static <V> SimpleLoadingCache<V> newGuavaLoadingCache(int duration, String name, Function<String, V> loader) {
    return new GuavaLoadingCache<>(duration, name, loader);
  }

  /**
   * Creates new classic Redis (distributed) cache
   *
   * @param duration duration of the entries
   * @param redisURI URI of the redis node to connect to
   * @param name name of the cache - to be identified in logs and isolated from others in Redis
   * @param type datatype of the entries
   * @param loader function that loads in object that is not present in the cahce
   * @return the instantiated cache
   * @param <V> type of the entries stored in the cache
   */
  public static <V> SimpleLoadingCache<V> newRedisLoadingCache(int duration, String redisURI, String name, Class<V> type, Function<String, V> loader) {
    return new RedisLoadingCache<>(duration, redisURI, name, type, loader);
  }

  /**
   * Creates new Redis (distributed) cache which entries do not expire per se. Instead, they are stored permanently
   * in Redis and, when the duration time is surpassed, they are updated asynchronously in the background
   *
   * @param duration duration of the entries
   * @param redisURI URI of the redis node to connect to
   * @param name name of the cache - to be identified in logs and isolated from others in Redis
   * @param type datatype of the entries
   * @param loader function that loads in object that is not present in the cahce
   * @return the instantiated cache
   * @param <V> type of the entries stored in the cache
   */
  public static <V> SimpleLoadingCache<V> newRedisAsyncLoadingCache(int duration, String redisURI, String name, JavaType type, Function<String, V> loader) {
    return new RedisAsyncLoadingCache<>(duration, redisURI, name, type, loader);
  }

  /**
   * Creates new classic Redis (distributed) cache In addition, to avoid marshalling and communication costs,
   * this cache is also backed by a Guava one, which stores the entries in memory for a particular period of time
   *
   * @param guavaDuration duration of the entries in the Guava in-memory cache
   * @param redisDuration duration of the entries in the Redis cache
   * @param redisURI URI of the redis node to connect to
   * @param name name of the cache - to be identified in logs and isolated from others in Redis
   * @param type datatype of the entries
   * @param loader function that loads in object that is not present in the cahce
   * @return the instantiated cache
   * @param <V> type of the entries stored in the cache
   */
  public static <V> SimpleLoadingCache<V> newGuavaBackedRedisCache(int guavaDuration, int redisDuration, String redisURI, String name, Class<V> type,
      Function<String, V> loader) {
    // we create a Redis cache that is wrapped by a Guava one
    SimpleLoadingCache<V> redisCache = newRedisLoadingCache(redisDuration, redisURI, name, type, loader);
    SimpleLoadingCache<V> guavaCache = newGuavaLoadingCache(guavaDuration, name, redisCache::getUnchecked);

    // if an invalidation is done to the Guava cache, it is propagated to the Redis one
    guavaCache.addInvalidationListener(redisCache::invalidate);

    return guavaCache;
  }

  /**
   * Creates new Redis (distributed) cache which entries do not expire per se. Instead, they are stored permanently
   * in Redis and, when the duration time is surpassed, they are updated asynchronously in the background. In addition,
   * to avoid marshalling and communication costs, this cache is also backed by a Guava one, which stores the entries
   * in memory for a particular period of time
   *
   * @param guavaDuration duration of the entries in the Guava in-memory cache
   * @param redisDuration duration of the entries in the Redis cache
   * @param redisURI URI of the redis node to connect to
   * @param name name of the cache - to be identified in logs and isolated from others in Redis
   * @param type datatype of the entries
   * @param loader function that loads in object that is not present in the cahce
   * @return the instantiated cache
   * @param <V> type of the entries stored in the cache
   */
  public static <V> SimpleLoadingCache<V> newGuavaBackedRedisAsyncCache(int guavaDuration, int redisDuration, String redisURI, String name, JavaType type,
      Function<String, V> loader) {
    // we create a Redis cache that is wrapped by a Guava one
    SimpleLoadingCache<V> redisCache = newRedisAsyncLoadingCache(redisDuration, redisURI, name, type, loader);
    SimpleLoadingCache<V> guavaCache = newGuavaLoadingCache(guavaDuration, name, redisCache::getUnchecked);

    // if an invalidation is done to the Guava cache, it is propagated to the Redis one
    guavaCache.addInvalidationListener(redisCache::invalidate);

    return guavaCache;
  }

  public static JavaType listType(Class<?> type) {
    return JSONSerializer.listType(type);
  }

  public static JavaType type(Class<?> type) {
    return JSONSerializer.type(type);
  }

  public static JavaType parameterizedType(Class<?> wrapperType, JavaType internalType) {
    return JSONSerializer.parameterizedType(wrapperType, internalType);
  }

}
