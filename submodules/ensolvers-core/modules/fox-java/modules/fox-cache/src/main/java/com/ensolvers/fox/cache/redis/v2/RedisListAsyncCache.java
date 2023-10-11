package com.ensolvers.fox.cache.redis.v2;

import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.cache.redis.RedisCache;
import com.ensolvers.fox.services.util.FailableFunction;
import com.ensolvers.fox.services.util.KeyBasedExecutor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import io.lettuce.core.RedisNoScriptException;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Simple redis cache that stores DTOs as strings serializing it using Jackson. - The cache has a loading function to
 * load the list of values of the missed keys. - The loading of the cache is atomic, that means that if more than 1
 * thread is trying to access to a missed key, one will execute the loader and the rest will wait until the operation is
 * done. - If the loader returns null, the cache stores the "null" string, and returns null value when accessing it. -
 * If when a value is retrieved the key reaches the logical expiration limit, the loader runs asynchronously to refresh
 * the value in the cache before it expires.
 *
 * NOTE: This cache is depreacted since it shown erratic behaviour in production, delaying Redis requests for several
 * seconds. We suspect that it is due to the use of Lua scripts. Please use variants under the `v3` package
 */

@Deprecated(since = "2023-07-30", forRemoval = true)
public class RedisListAsyncCache<K, V> extends RedisCacheV2<K, V> {
  private static final String SERIALIZATION_PROBLEM = "There was a problem during serialization";

  // If the list to store is empty we store the key with the suffix "-is-empty" because redis does not support empty lists
  private static final String REDIS_EMPTY_LIST_SUFFIX = "-is-empty";

  private final FailableFunction<K, List<V>, Exception> loader;
  private final int remainingTTLLimitInSeconds;
  private final KeyBasedExecutor keyBasedExecutor = new KeyBasedExecutor();
  private String atomicAsyncPushScriptSha;

  public RedisListAsyncCache(RedisCommands<String, String> redis, String cacheName, int ttlInSeconds, int logicalTTLInSeconds, Class<V> valueClass,
      Function<K, String> keyBuilder, FailableFunction<K, List<V>, Exception> loader) {
    super(redis, cacheName, ttlInSeconds, valueClass, keyBuilder, null, null);
    this.loader = loader;
    this.remainingTTLLimitInSeconds = ttlInSeconds - logicalTTLInSeconds;
    if (this.remainingTTLLimitInSeconds <= 0) {
      throw new CacheExecutionException("logicalTTL is greater than physical ttl");
    }
    this.loadAtomicPushScript();
  }

  public RedisListAsyncCache(RedisCommands<String, String> redis, String cacheName, int ttlInSeconds, int logicalTTLInSeconds, JavaType javaType,
      Function<K, String> keyBuilder, FailableFunction<K, List<V>, Exception> loader) {
    super(redis, cacheName, ttlInSeconds, javaType, keyBuilder, null, null);
    this.loader = loader;
    this.remainingTTLLimitInSeconds = ttlInSeconds - logicalTTLInSeconds;
    if (this.remainingTTLLimitInSeconds <= 0) {
      throw new CacheExecutionException("logicalTTL is greater than physical ttl");
    }
    this.loadAtomicPushScript();
  }

  private void loadAtomicPushScript() {
    ClassLoader classLoader = RedisListAsyncCache.class.getClassLoader();
    try (InputStream stream = classLoader.getResourceAsStream("redis.lua.scripts/core_atomic_async_push.lua")) {
      byte[] atomicPushScriptContent = stream.readAllBytes();
      this.atomicAsyncPushScriptSha = redis.scriptLoad(atomicPushScriptContent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<V> get(K key) {
    notNull(key);
    String computedKey = this.computeKey(key);

    // If logical ttl reached and the key is not already associated with a value
    if (this.isLogicalTTLReached(computedKey)) {
      keyBasedExecutor.runThreadBasedOnKey(computedKey, () -> this.executeLoaderWithSilentlyThrows(key, computedKey));
    }

    return this.get(key, 0);
  }

  public void del(K key, V value) {
    try {
      this.redis.lrem(this.computeKey(key), 0, this.serializeValue(value));
    } catch (JsonProcessingException e) {
      throw new CacheSerializingException(SERIALIZATION_PROBLEM, e);
    } catch (Exception e) {
      throw new CacheExecutionException("Error trying to execute del", e);
    }
  }

  public void del(K key, Collection<V> values) {
    values.forEach(value -> this.del(key, value));
  }

  public Long size(K key) {
    return this.redis.llen(this.computeKey(key));
  }

  private List<V> get(K key, int retries) {
    if (retries >= 5) {
      throw new CacheExecutionException("Reached max get retries");
    }
    retries++;

    try {
      notNull(key);
      String computedKey = this.computeKey(key);

      // We check if a key indicating the list is empty exists (because redis does not store empty lists)
      if (this.redis.exists(computedKey + REDIS_EMPTY_LIST_SUFFIX) > 0L) {
        return new ArrayList<>();
      }

      List<String> representationList = this.redis.lrange(computedKey, 0L, -1L);

      // If the key is not present (empty list), we load it again and recursively call this method again
      if (representationList.isEmpty()) {
        this.executeLoader(key, computedKey, false);
        return this.get(key, retries);
      }

      List<V> result = new ArrayList<>(representationList.size());
      for (String representation : representationList) {
        result.add(this.deserializeValue(representation));
      }

      return result;
    } catch (RedisNoScriptException e) {
      // If the script was removed, we load it again and recursively call this method again
      loadAtomicPushScript();
      return this.get(key, retries);
    } catch (IOException e) {
      throw new CacheSerializingException(SERIALIZATION_PROBLEM, e);
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
      if ((Boolean.FALSE.equals(this.keyExists(key)) && this.redis.exists(computedKey + REDIS_EMPTY_LIST_SUFFIX) == 0L)
          || (isAsync && this.isLogicalTTLReached(computedKey))) {
        List<V> values = this.loader.apply(key);
        redis.evalsha(atomicAsyncPushScriptSha, ScriptOutputType.BOOLEAN,
            // key
            new String[] { computedKey, String.valueOf(expirationTime), String.valueOf(isAsync) },
            // other arguments
            this.collectionOfVToStringArray(values));
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
