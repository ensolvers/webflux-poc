package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RedisSetCache<V> extends RedisCache<V> implements RedisCollection<V> {

  public RedisSetCache(RedisCommands<String, String> redis, String cacheName, int expirationTime, Class<V> valueClass,
      CheckedFunction<V, String> customSerializer, CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    super(redis, cacheName, expirationTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  @Override
  public Set<V> get(String key) {
    try {
      Set<V> result = new HashSet<>();
      Set<String> representationList = this.redis.smembers(this.computeKey(key));
      for (String representation : representationList) {
        result.add(this.deserializeValue(representation));
      }
      return result;
    } catch (IOException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    }
  }

  @Override
  public void del(String key, V value) {
    this.del(key, Collections.singletonList(value));
  }

  @Override
  public void del(String key, Collection<V> values) {
    try {
      this.redis.srem(this.computeKey(key), this.collectionOfVToStringArray(values));
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException("There was a problem during serialization", e);
    }
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
      this.redis.sadd(this.computeKey(key), this.collectionOfVToStringArray(values));
      this.redis.expire(this.computeKey(key), expirationTime);
      return null;
    });
  }

  @Override
  public Long size(String key) {
    return this.redis.scard(this.computeKey(key));
  }
}
