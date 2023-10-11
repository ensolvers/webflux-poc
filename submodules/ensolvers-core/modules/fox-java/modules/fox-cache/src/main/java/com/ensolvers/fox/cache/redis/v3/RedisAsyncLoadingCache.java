package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.cache.Caches;
import com.ensolvers.fox.cache.guava.AbstractSimpleLoadingCache;
import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.util.JSONSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class RedisAsyncLoadingCache<V> extends AbstractSimpleLoadingCache<V> {

  private final String cacheName;
  private final Function<String, V> loader;
  private final JavaType type;
  private final int duration;
  private final RMapCache<String, String> map;
  private final ExecutorService loaderExecutor;
  private RedissonClient client;
  private static final Logger logger = CoreLogger.getLogger(RedisAsyncLoadingCache.class);

  public RedisAsyncLoadingCache(int duration, String redisURI, String name, Class<V> type, Function<String, V> loader) {
    this(duration, redisURI, name, Caches.type(type), loader);
  }

  public RedisAsyncLoadingCache(int duration, String redisURI, String name, JavaType javaType, Function<String, V> loader) {
    Config config = new Config();
    config.useSingleServer().setSubscriptionsPerConnection(20);
    config.useSingleServer().setAddress(redisURI);
    this.client = Redisson.create(config);
    this.cacheName = name;
    this.loader = loader;
    this.type = Caches.parameterizedType(CacheEntry.class, javaType);
    this.duration = duration;
    this.map = this.client.getMapCache(RedisAsyncLoadingCache.class.getName() + "-" + this.cacheName);
    this.loaderExecutor = Executors.newFixedThreadPool(3);
  }

  @Override
  public V getUnchecked(String key) {
    String serializedObject = this.map.get(key);

    try {
      if (serializedObject == null) {
        return this.loadAndReturn(key);
      } else {
        CacheEntry<V> entry = JSONSerializer.get().deserialize(serializedObject, this.type);

        if ((new Date().getTime() - entry.getTimestamp()) / 1000 > this.duration) {
          logger.info("Loading key asynchronously, cache [" + this.cacheName + "], key [" + key + "]");
          this.loaderExecutor.submit(() -> this.loadAndReturn(key));
        }

        return entry.getObject();
      }
    } catch (Exception e) {
      throw new RuntimeException("Exception when trying to get/serialize object, cache [" + this.cacheName + "]" + ", key [" + key + "]", e);
    }
  }

  private V loadAndReturn(String key) throws JsonProcessingException {
    V object = null;

    try {
      object = this.loader.apply(key);
    } catch (Exception e) {
      // if a non-checked exception occurs, we ensure that it is logged properly
      logger.error("Error when trying to fetch content, cache [" + this.cacheName + "], key [" + key + "]", e);
      throw e;
    }
    this.map.put(key, JSONSerializer.get().serialize(new CacheEntry<>(new Date().getTime(), object)));
    logger.info("Key loaded and stored, cache [" + this.cacheName + "], key [" + key + "]");
    return object;
  }

  @Override
  public void invalidateEntry(String key) {
    this.map.remove(key);
  }

  @Getter
  private static class CacheEntry<V> {

    public CacheEntry() {
    }

    public CacheEntry(long timestamp, V object) {
      this.timestamp = timestamp;
      this.object = object;
    }

    private long timestamp;
    private V object;

  }

}
