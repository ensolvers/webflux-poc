package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.util.JSONSerializer;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RedisConsumableMapCache<V> {
  private final Class<V> objectType;
  private final Function<String, Map<String, V>> provider;
  private final int expirationInSeconds;
  private final RedissonClient client;

  private static final Logger logger = CoreLogger.getLogger(RedisConsumableMapCache.class);

  public RedisConsumableMapCache(String redisURI, Class<V> objectType, Function<String, Map<String, V>> provider, int expirationInSeconds) {
    Config config = new Config();
    config.useSingleServer().setAddress(redisURI);
    this.client = Redisson.create(config);
    this.objectType = objectType;
    this.provider = provider;
    this.expirationInSeconds = expirationInSeconds;
  }

  private String getMapName(String mapKey) {
    return RedisConsumableMapCache.class.getName() + "-Map-" + mapKey;
  }

  private String getMapLoadingSemaphoreName(String mapKey) {
    return RedisConsumableMapCache.class.getName() + "-Semaphore-" + mapKey;
  }

  private String getLockKeyName(String mapKey, String entryKey) {
    return RedisConsumableMapCache.class.getName() + "-Lock-" + mapKey + "-" + entryKey;
  }

  private RMap<String, Boolean> getConsumedMap(String mapKey) {
    String consumedMapName = RedisConsumableMapCache.class.getName() + "-Consumed-" + mapKey;
    return this.client.getMap(consumedMapName);
  }

  private RMap<String, String> getMap(String mapKey) throws Exception {
    RMap<String, String> map = this.client.getMap(getMapName(mapKey));

    // if the map does not exist...
    if (!map.isExists()) {
      logger.info("Map [{}] does not exists, trying to grab lock for loading", mapKey);

      // first we ensure that only one thread/node can fetch the map through a semaphore
      RSemaphore semaphore = this.client.getSemaphore(getMapLoadingSemaphoreName(mapKey));
      semaphore.trySetPermits(1);
      semaphore.acquire();

      try {
        // now, at this point only one thread/node must be executing this, we ensure
        // that the map was still not loaded by other thread/node
        map = this.client.getMap(getMapName(mapKey));

        // if still not, we proceed to loading
        if (!map.isExists()) {
          logger.info("Loading content for [{}]", mapKey);

          map.clear();

          // we create the map, add dummy entry to ensure is not treated by Redisson as a non-existent object,
          // store al entries by serializing the values and finally we set expiration time
          Map<String, V> finalMap = this.provider.apply(mapKey);
          finalMap.put("__dummy_key", this.objectType.getConstructor().newInstance());
          map.putAll(JSONSerializer.get().serializeMapValues(finalMap));
          map.expire(Instant.now().plusSeconds(this.expirationInSeconds));

          this.intializeConsumedMap(mapKey, finalMap);

          logger.info("Content loaded for [{}]", mapKey);
        }
      } finally {
        semaphore.release();
      }
    }

    return map;
  }

  private void intializeConsumedMap(String mapKey, Map<String, V> originalMap) {
    RMap<String, Boolean> consumedMap = this.getConsumedMap(mapKey);

    Map<String, Boolean> initializedConsumedMap = originalMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, value -> false));

    consumedMap.putAll(initializedConsumedMap);
  }

  public Map<String, V> consumeEntries(String mapKey, Set<String> keys) throws Exception {
    RMap<String, String> map = this.getMap(mapKey);

    Map<String, Boolean> consumed = this.getConsumedMap(mapKey).getAll(keys);

    // we check that all the values are still available - all of them are false
    if (consumed.values().stream().noneMatch(value -> value)) {
      // we then mark all of them as consumed
      consumed.replaceAll((key, value) -> true);
      this.getConsumedMap(mapKey).putAll(consumed);

      // finally, we return all keys
      return JSONSerializer.get().deserializeMapValues(map.getAll(keys), this.objectType);
    }

    // on the other hand, if some values are consumed already, we just free all and throw an exception
    consumed.replaceAll((key, value) -> false);
    throw new Exception("Some of the values are consumed already");
  }

  public void releaseEntries(String mapKey, Set<String> keys) {
    Map<String, Boolean> consumed = this.getConsumedMap(mapKey).getAll(keys);
    consumed.replaceAll((key, value) -> false);
    this.getConsumedMap(mapKey).putAll(consumed);
  }

}
