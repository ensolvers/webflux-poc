package com.ensolvers.fox.cache.redis;

import io.lettuce.core.api.sync.RedisCommands;
import java.util.Collection;
import java.util.Collections;

public class RedisLimitedCache<V> extends RedisListCache<V> {

  public RedisLimitedCache(RedisCommands<String, String> redis, String name, int expirationTime, Class<V> valueClass,
      CheckedFunction<V, String> customSerializer, CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    super(redis, name, expirationTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  /**
   * Pushes an element to a non-expiring list cache that is clipped to the maxEntriesPerBlock
   *
   * @param key   The key of the collection.
   * @param value to be added
   */
  @Override
  public void push(String key, V value) {
    this.push(key, value, false);
  }

  /**
   * Pushes a collection of elements to a non-expiring list cache that is clipped to the maxEntriesPerBlock
   *
   * @param key    The key of the collection.
   * @param values to be added.
   */
  @Override
  public void push(String key, Collection<V> values) {
    this.push(key, values, false);
  }

  /**
   * Pushes an element to a list cache that is clipped to the maxEntriesPerBlock
   *
   * @param key    of the collection
   * @param value  to be added
   * @param expire Add expiration time
   */
  public void push(String key, V value, boolean expire) {
    notNull(value);
    this.push(key, Collections.singletonList(value), expire);
  }

  /**
   * Pushes an element to a list cache that is clipped to the maxEntriesPerBlock
   *
   * @param key    of the collection
   * @param values to be added
   * @param expire Add expiration time
   */
  public void push(String key, Collection<V> values, boolean expire) {
    notNull(key);
    notEmpty(values);

    boolean keyExists = this.keyExists(key);
    boolean overflowEntries = this.size(key) >= this.maxEntriesPerBlock;

    this.redisTransaction(() -> {
      // Takes the first element if block size reached
      if (keyExists && overflowEntries) {
        this.redis.rpop(this.computeKey(key));
      }

      this.redis.lpush(this.computeKey(key), this.collectionOfVToStringArray(values));

      if (expire) {
        this.redis.expire(this.computeKey(key), expirationTime);
      }

      return null;
    });
  }
}
