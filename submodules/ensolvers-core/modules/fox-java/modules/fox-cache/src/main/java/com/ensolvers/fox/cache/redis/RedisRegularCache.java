package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.cache.common.GenericCache;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.cache.redis.v2.RedisRegularAsyncCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;

/**
 * Deprecated. Use {@link RedisRegularAsyncCache} instead
 */
@Deprecated(since = "2022-12-16")
public class RedisRegularCache<V> extends RedisCache<V> implements GenericCache<V> {

  public RedisRegularCache(RedisCommands<String, String> redis, String name, int expirationTime, Class<V> valueClass,
      CheckedFunction<V, String> customSerializer, CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    super(redis, name, expirationTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  /**
   * Given a key returns the associated value. Return null if there's no such key.
   *
   * @param key The key of the element to retrieve.
   * @return The value associated with the key.
   */
  public V get(String key) {
    try {
      return this.deserializeValue(this.redis.get(this.computeKey(key)));
    } catch (IOException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    }
  }

  /**
   * Sets a value to a key. Overrides previous value if key already exists. Sets TTL.
   *
   * @param key   The key of the element to store.
   * @param value The element to store.
   */
  public void set(String key, V value) {
    notNull(key);
    notNull(value);
    try {
      this.redis.set(this.computeKey(key), this.serializeValue(value), new SetArgs().ex(expirationTime));
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    }
  }

  @Override
  public void put(String key, V object) {
    this.set(key, object);
  }
}
