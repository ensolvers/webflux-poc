package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.cache.Caches;
import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import com.ensolvers.fox.cache.guava.AbstractSimpleLoadingCache;
import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.util.KeyBasedExecutor;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class RedisAtomicLongCache extends AbstractSimpleLoadingCache<Long> {

  private static Logger logger = CoreLogger.getLogger(RedisAtomicLongCache.class);

  private final RedissonClient client;
  private final Function<String, Long> provider;
  private final int expirationInSeconds;
  private final String name;

  private final ConcurrentMap<String, RAtomicLong> atomicLongByKey;

  public RedisAtomicLongCache(String redisURI, String name, Function<String, Long> provider, int expirationInSeconds) {
    Config config = new Config();
    config.useSingleServer().setAddress(redisURI);
    this.name = name;
    this.client = Redisson.create(config);
    this.provider = provider;
    this.expirationInSeconds = expirationInSeconds;
    this.atomicLongByKey = new ConcurrentHashMap<>();
  }

  private RAtomicLong getAtomicLongReference(String key) {
    String finalKey = this.name + "-" + key;

    return this.atomicLongByKey.computeIfAbsent(key, k -> {
      RAtomicLong atomicLong = this.client.getAtomicLong(finalKey);

      if (!atomicLong.isExists()) {
        atomicLong.set(this.provider.apply(k));
      }

      return atomicLong;
    });
  }

  public long addAndGet(String key, long delta) {
    return this.getAtomicLongReference(key).addAndGet(delta);
  }

  @Override
  public Long getUnchecked(String key) {
    return this.getAtomicLongReference(key).get();
  }

  @Override
  protected void invalidateEntry(String key) {
    this.getAtomicLongReference(key).delete();
  }

  public void set(String key, long value) {
    this.getAtomicLongReference(key).set(value);
  }
}
