package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import com.ensolvers.fox.cache.guava.AbstractSimpleLoadingCache;
import com.ensolvers.fox.services.util.JSONSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RedisLoadingCache<V> extends AbstractSimpleLoadingCache<V> {

  private final String cacheName;
  private final Function<String, V> loader;
  private final Class<V> type;
  private final int duration;
  private final RMapCache<String, String> map;
  private RedissonClient client;
  private static final Logger logger = LoggerFactory.getLogger(RedisLoadingCache.class);

  public RedisLoadingCache(int duration, String redisURI, String name, Class<V> type, Function<String, V> loader) {
    Config config = new Config();
    config.useSingleServer().setSubscriptionsPerConnection(20);
    config.useSingleServer().setAddress(redisURI);
    this.client = Redisson.create(config);
    this.cacheName = name;
    this.loader = loader;
    this.type = type;
    this.duration = duration;
    this.map = this.client.getMapCache(RedisLoadingCache.class.getName() + "-" + this.cacheName);
  }

  @Override
  public V getUnchecked(String key) {
    String serializedObject = this.map.get(key);

    if (serializedObject == null) {
      try {
        return executeLoaderAtomically(key);
      } catch (Exception e) {
        throw new RuntimeException("Error when trying to serialize object for key [" + key + "], cache [" + this.cacheName + "]", e);
      }
    }

    try {
      return JSONSerializer.get().deserialize(serializedObject, this.type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error when trying to deserialize object for key [" + key + "], cache [" + this.cacheName + "]", e);
    }
  }

  private V executeLoaderAtomically(String key) throws Exception {
    // we ensure that only one thread/node is loading at a time via a lock
    RLock lock = this.client.getLock(RedisLoadingCache.class.getName() + "-" + this.cacheName + "-Lock-" + key);
    lock.lock(20, TimeUnit.SECONDS);
    logger.info("Fetching lock acquired for cache [{}], key [{}]", cacheName, key);

    // if we are the second or n-th thread-node that is waiting for the lock, the content was probably loaded
    // by a previous one, so we do that check
    String content = this.map.get(key);
    V resultingObject = null;

    // if no content (we assume that we are the first thread on reaching this point) we execute the loader
    // and store the key in them map
    if (content == null) {
      resultingObject = this.loader.apply(key);
      map.put(key, JSONSerializer.get().serialize(resultingObject), this.duration, TimeUnit.SECONDS);
    } else {
      // there is content in the entry, another thread/node loaded it, we just deserialize the object
      resultingObject = JSONSerializer.get().deserialize(content, this.type);
    }

    // we free the lock and return the object
    lock.unlock();
    logger.info("Fetching lock released for cache [{}], key [{}]", cacheName, key);

    return resultingObject;
  }

  @Override
  public void invalidateEntry(String key) {
    this.map.remove(key);
  }

}
