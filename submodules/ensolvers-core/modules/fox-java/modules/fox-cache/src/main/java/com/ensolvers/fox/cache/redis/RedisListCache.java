package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Deprecated. Use {@link com.ensolvers.fox.cache.redis.v2.RedisListAsyncCache} instead
 */
public class RedisListCache<V> extends RedisCache<V> implements RedisCollection<V> {
  private static final String SERIALIZATION_PROBLEM = "There was a problem during serialization";

  public RedisListCache(RedisCommands<String, String> redis, String name, int expirationTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    super(redis, name, expirationTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  @Override
  public List<V> get(String key) {
    try {
      List<V> result = new ArrayList<>();
      List<String> representationList = this.redis.lrange(this.computeKey(key), 0L, -1L);
      for (String representation : representationList) {
        result.add(this.deserializeValue(representation));
      }
      return result;
    } catch (Exception e) {
      throw new CacheSerializingException(SERIALIZATION_PROBLEM, e);
    }
  }

  /**
   * Remove and get the first element of the list.
   *
   * @param key The key of the list.
   * @return the first element of the list.
   */
  public V pop(String key) {
    try {
      return this.deserializeValue(this.redis.lpop(this.computeKey(key)));
    } catch (IOException e) {
      throw new CacheSerializingException(SERIALIZATION_PROBLEM, e);
    }
  }

  @Override
  public void del(String key, V value) {
    try {
      this.redis.lrem(this.computeKey(key), 0, this.serializeValue(value));
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException(SERIALIZATION_PROBLEM, e);
    }
  }

  @Override
  public void del(String key, Collection<V> values) {
    values.forEach(value -> this.del(key, value));
  }

  @Override
  public void push(String key, V value) {
    notNull(value);
    this.push(key, Collections.singletonList(value));
  }

  @Override
  public void push(String key, Collection<V> values) {
    notNull(key);
    notEmpty(values);
    this.redisTransaction(() -> {
      this.redis.lpush(this.computeKey(key), this.collectionOfVToStringArray(values));
      this.redis.expire(this.computeKey(key), expirationTime);
      return null;
    });
  }

  @Override
  public Long size(String key) {
    return this.redis.llen(this.computeKey(key));
  }

  /**
   * Append a value to a list.
   *
   * @param key
   * @param value
   */
  public void append(String key, V value) {
    this.append(key, Collections.singletonList(value));
  }

  /**
   * Append multiple values to a list.
   *
   * @param key
   * @param values
   */
  public void append(String key, List<V> values) {
    notNull(key);
    notEmpty(values);
    this.redisTransaction(() -> {
      this.redis.rpush(this.computeKey(key), this.collectionOfVToStringArray(values));
      this.redis.expire(this.computeKey(key), expirationTime);
      return null;
    });
  }
}
