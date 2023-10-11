package com.ensolvers.fox.cache.redis.v2;

import com.ensolvers.fox.cache.common.GenericCacheV2;
import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.services.util.FailableFunction;
import com.ensolvers.fox.services.util.KeyBasedExecutor;
import com.fasterxml.jackson.databind.JavaType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.IOException;
import java.util.function.Function;

/**
 * Simple redis cache that stores DTOs as strings serializing it using Jackson. - The cache has a loading function to
 * load the value of the missed keys. - The loading of the cache is atomic, that means that if more than 1 thread is
 * trying to access to a missed key, one will execute the loader and the rest will wait until the operation is done. -
 * If the loader returns null, the cache stores the "null" string, and returns null value when accessing it. - If when a
 * value is retrieved the key reaches the logical expiration limit, the loader runs asynchronously to refresh the value
 * in the cache before it expires.
 *
 * NOTE: This cache is deprecated since the TTL implementation it invalidates the resources eventually.
 * Use alternatives in the `v3` package like {@link com.ensolvers.fox.cache.redis.v3.RedisAsyncLoadingCache}
 */

@Deprecated(since = "2023-07-30", forRemoval = true)
public class RedisRegularAsyncCache<K, V> extends RedisCacheV2<K, V> implements GenericCacheV2<K, V> {
  private final FailableFunction<K, V, Exception> loader;
  private final int remainingTTLLimitInSeconds;
  private final KeyBasedExecutor keyBasedExecutor = new KeyBasedExecutor();

  public RedisRegularAsyncCache(RedisCommands<String, String> redis, String cacheName, int ttlInSeconds, int logicalTTLInSeconds, Class<V> valueClass,
      Function<K, String> keyBuilder, FailableFunction<K, V, Exception> loader) {
    super(redis, cacheName, ttlInSeconds, valueClass, keyBuilder, null, null);
    this.loader = loader;
    this.remainingTTLLimitInSeconds = ttlInSeconds - logicalTTLInSeconds;
    if (this.remainingTTLLimitInSeconds <= 0) {
      throw new CacheExecutionException("logicalTTL is greater than physical ttl");
    }
  }

  public RedisRegularAsyncCache(RedisCommands<String, String> redis, String cacheName, int ttlInSeconds, int logicalTTLInSeconds, JavaType javaType,
      Function<K, String> keyBuilder, FailableFunction<K, V, Exception> loader) {
    super(redis, cacheName, ttlInSeconds, javaType, keyBuilder, null, null);
    this.loader = loader;
    this.remainingTTLLimitInSeconds = ttlInSeconds - logicalTTLInSeconds;
    if (this.remainingTTLLimitInSeconds <= 0) {
      throw new CacheExecutionException("logicalTTL is greater than physical ttl");
    }
  }

  /**
   * Given a key returns the associated value. Return null if there's no such key.
   *
   * @param key The key of the element to retrieve.
   * @return The value associated with the key.
   */
  @Override
  public V get(K key) {
    notNull(key);
    String computedKey = this.computeKey(key);

    // If logical ttl reached and the key is not already associated with a value
    if (this.isLogicalTTLReached(computedKey)) {
      keyBasedExecutor.runThreadBasedOnKey(computedKey, () -> this.executeLoaderWithSilentlyThrows(key, computedKey));
    }

    return this.get(key, 0);
  }

  @Override
  public void put(K key, V value) {
    notNull(key);
    try {
      this.redis.set(this.computeKey(key), this.serializeValue(value), (new SetArgs()).ex(this.expirationTime));
    } catch (Exception e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    }
  }

  private V get(K key, int retries) {
    if (retries >= 5) {
      throw new CacheExecutionException("Reached max get retries");
    }
    retries++;

    try {
      String computedKey = this.computeKey(key);
      String serializedValue = this.redis.get(computedKey);

      if (serializedValue == null) {
        this.executeLoader(key, computedKey, false);
        return this.get(key, retries);
      }

      return this.deserializeValue(serializedValue);
    } catch (IOException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    } catch (Exception e) {
      throw new CacheExecutionException("Error trying to execute async loader", e);
    }
  }

  /**
   * Synchronized method to call the loader once, the rest of the threads will wait until the load is finished
   * 
   * @param key the key of the object to store
   */
  private void executeLoader(K key, String computedKey, boolean isAsync) throws Exception {
    keyBasedExecutor.runJustOnceBasedOnKey(computedKey, () -> {
      // We check that the key not exists
      if (Boolean.FALSE.equals(this.keyExists(key)) || (isAsync && this.isLogicalTTLReached(computedKey))) {
        V value = this.loader.apply(key);
        this.redis.set(computedKey, this.serializeValue(value), (new SetArgs()).ex(this.expirationTime));
      }
    });
  }

  private void executeLoaderWithSilentlyThrows(K key, String computedKey) {
    try {
      this.executeLoader(key, computedKey, true);
    } catch (IOException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    } catch (Exception e) {
      throw new CacheExecutionException("Error trying to execute async loader", e);
    }
  }

  private boolean isLogicalTTLReached(String computedKey) {
    // If ttl negative, the key not exists or not have expiration time associated
    Long ttl = this.redis.ttl(computedKey);
    return ttl > 0 && ttl < this.remainingTTLLimitInSeconds;
  }
}
