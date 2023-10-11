package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.cache.exception.CacheInitializationException;
import com.ensolvers.fox.cache.redis.v2.RedisCacheV2;
import com.ensolvers.fox.cache.redis.v2.RedisListAsyncCache;
import com.ensolvers.fox.cache.redis.v2.RedisRegularAsyncCache;
import com.ensolvers.fox.services.util.FailableFunction;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RedisCacheFactory {
  private final RedisClient client;
  private final StatefulRedisConnection<String, String> connection;
  private final RedisCommands<String, String> redis;
  protected List<String> caches;

  public RedisCacheFactory(RedisClient client) {
    caches = new ArrayList<>();
    this.client = client;
    this.connection = client.connect();
    this.redis = connection.sync();
  }

  /**
   * Creates a new RedisRegularCache.
   *
   * @param name       of the cache, serves as "topic"
   * @param expireTime time in seconds for the elements in the cache to expire.
   * @param valueClass Class of the values.
   * @param <V>        Class of the values.
   */
  public <V> RedisRegularCache<V> getRegularCache(String name, int expireTime, Class<V> valueClass) {
    return this.getCache(name, expireTime, valueClass, RedisRegularCache.class, null, null, null);
  }

  /**
   * Creates a new RedisRegularAsyncCache.
   *
   * @param name              of the cache, serves as "topic".
   * @param expireTime        time in seconds for the elements in the cache to expire.
   * @param logicalExpireTime logical time in seconds for the elements in the cache to expire.
   * @param loader            cache loader function.
   * @param valueClass        Class of the values.
   * @param <V>               Class of the values.
   */
  public <K, V> RedisRegularAsyncCache<K, V> getRegularAsyncCache(String name, int expireTime, int logicalExpireTime, Function<K, String> keyBuilder,
      FailableFunction<K, V, Exception> loader, Class<V> valueClass) {
    return this.getCache(name, expireTime, logicalExpireTime, valueClass, RedisRegularAsyncCache.class, keyBuilder, loader);
  }

  /**
   * Creates a new RedisRegularCache
   *
   * @param name               of the cache, serves as "topic"
   * @param expireTime         time in seconds for the elements in the cache to expire.
   * @param valueClass         Class of the values.
   * @param <V>                Class of the values.
   * @param customSerializer   Serializer
   * @param customDeserializer Deserializer
   */
  public <V> RedisRegularCache<V> getRegularCache(String name, int expireTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer) {
    return this.getCache(name, expireTime, valueClass, RedisRegularCache.class, customSerializer, customDeserializer, null);
  }

  /**
   * Creates a new RedisListCache.
   *
   * @param name       of the cache, serves as "topic"
   * @param expireTime time in seconds for the elements in the cache to expire.
   * @param valueClass Class of the values.
   * @param <V>        Class of the values.
   */
  public <V> RedisListCache<V> getListCache(String name, int expireTime, Class<V> valueClass) {
    return this.getCache(name, expireTime, valueClass, RedisListCache.class, null, null, null);
  }

  /**
   * Creates a new RedisRegularAsyncCache.
   *
   * @param name              of the cache, serves as "topic".
   * @param expireTime        time in seconds for the elements in the cache to expire.
   * @param logicalExpireTime logical time in seconds for the elements in the cache to expire.
   * @param loader            cache loader function.
   * @param valueClass        Class of the values.
   * @param <V>               Class of the values.
   */
  public <V> RedisListAsyncCache<String, V> getListAsyncCache(String name, int expireTime, int logicalExpireTime,
      FailableFunction<String, List<V>, Exception> loader, Class<V> valueClass) {
    return this.getCacheList(name, expireTime, logicalExpireTime, valueClass, RedisListAsyncCache.class, loader);
  }

  /**
   * Creates a new RedisListCache with custom serializers.
   *
   * @param name       of the cache, serves as "topic"
   * @param expireTime time in seconds for the elements in the cache to expire.
   * @param valueClass Class of the values.
   * @param <V>        Class of the values.
   */
  public <V> RedisListCache<V> getListCache(String name, int expireTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer) {
    return this.getCache(name, expireTime, valueClass, RedisListCache.class, customSerializer, customDeserializer, null);
  }

  /**
   * Creates a new RedisLimitedListCache.
   *
   * @param name       of the cache, serves as "topic"
   * @param expireTime time in seconds for the elements in the cache to expire.
   * @param valueClass Class of the values.
   * @param <V>        Class of the values.
   */
  public <V> RedisLimitedCache<V> getLimitedListCache(String name, int expireTime, Class<V> valueClass, Integer maxEntriesPerBlock) {
    return this.getCache(name, expireTime, valueClass, RedisLimitedCache.class, null, null, maxEntriesPerBlock);
  }

  /**
   * Creates a new RedisLimitedListCache with custom serializers.
   *
   * @param name               of the cache, serves as "topic"
   * @param expireTime         time in seconds for the elements in the cache to expire.
   * @param valueClass         Class of the values.
   * @param <V>                Class of the values.
   * @param customSerializer   Serializer
   * @param customDeserializer Deserializer
   * @param maxEntriesPerBlock Max entries per block
   */
  public <V> RedisLimitedCache<V> getLimitedListCache(String name, int expireTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    return this.getCache(name, expireTime, valueClass, RedisLimitedCache.class, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  /**
   * Creates a new RedisSetCache if there are serializers for the Key class and Value Class.
   *
   * @param name       of the cache, serves as "topic"
   * @param expireTime time in seconds for the elements in the cache to expire.
   * @param valueClass Class of the values.
   * @param <V>        Class of the values.
   */
  public <V> RedisSetCache<V> getSetCache(String name, int expireTime, Class<V> valueClass) {
    return this.getCache(name, expireTime, valueClass, RedisSetCache.class, null, null, null);
  }

  /**
   * Creates a new RedisSetCache with custom serializers.
   *
   * @param name               of the cache, serves as "topic"
   * @param expireTime         time in seconds for the elements in the cache to expire.
   * @param valueClass         Class of the values.
   * @param <V>                Class of the values.
   * @param customSerializer   Serializer
   * @param customDeserializer Deserializer
   * @param maxEntriesPerBlock Max entries per block
   */
  public <V> RedisSetCache<V> getSetCache(String name, int expireTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    return this.getCache(name, expireTime, valueClass, RedisSetCache.class, customSerializer, customDeserializer, maxEntriesPerBlock);
  }

  /**
   * Creates a new cache if there's no other cache with the same name already created.
   *
   * @param name               of the cache, serves as "topic"
   * @param expireTime         time in seconds for the elements in the cache to expire.
   * @param valueClass         Type of the objects to be stored in the cache.
   * @param cacheType          Specific type of cache to be created.
   * @param customSerializer   custom serializer that will be used instead of default.
   * @param customDeserializer custom deserializer that will be used instead of default.
   * @return a new cache.
   */
  private <V, C extends RedisCache<V>> C getCache(String name, int expireTime, Class<V> valueClass, Class<C> cacheType,
      CheckedFunction<V, String> customSerializer, CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("Cache name cannot be empty");
    }
    if (!caches.contains(name)) {
      try {
        C cache = cacheType
            .getDeclaredConstructor(RedisCommands.class, String.class, int.class, Class.class, CheckedFunction.class, CheckedFunction.class, Integer.class)
            .newInstance(redis, name, expireTime, valueClass, customSerializer, customDeserializer, maxEntriesPerBlock);
        caches.add(name);
        return cache;
      } catch (Exception e) {
        throw new CacheInitializationException("There was a problem when initializing cache with name: " + name, e);
      }
    }
    throw new InvalidParameterException("Cache with name " + name + " already exist");
  }

  /**
   * Creates a new cache if there's no other cache with the same name already created.
   *
   * @param name              of the cache, serves as "topic"
   * @param expireTime        time in seconds for the elements in the cache to expire.
   * @param logicalExpireTime logical time in seconds for the elements in the cache to expire.
   * @param valueClass        Type of the objects to be stored in the cache.
   * @param cacheType         Specific type of cache to be created.
   * @param loader            cache loader function.
   * @return a new cache.
   */
  private <K, V, C extends RedisCacheV2<K, V>> C getCache(String name, int expireTime, int logicalExpireTime, Class<V> valueClass, Class<C> cacheType,
      Function<K, String> keyBuilder, FailableFunction<K, V, Exception> loader) {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("Cache name cannot be empty");
    }
    if (!caches.contains(name)) {
      try {
        C cache = cacheType.getDeclaredConstructor(RedisCommands.class, String.class, int.class, int.class, Class.class, Function.class, FailableFunction.class)
            .newInstance(redis, name, expireTime, logicalExpireTime, valueClass, keyBuilder, loader);
        caches.add(name);
        return cache;
      } catch (Exception e) {
        throw new CacheInitializationException("There was a problem when initializing cache with name: " + name, e);
      }
    }
    throw new InvalidParameterException("Cache with name " + name + " already exist");
  }

  /**
   * Creates a new cache if there's no other cache with the same name already created.
   *
   * @param name              of the cache, serves as "topic"
   * @param expireTime        time in seconds for the elements in the cache to expire.
   * @param logicalExpireTime logical time in seconds for the elements in the cache to expire.
   * @param valueClass        Type of the objects to be stored in the cache.
   * @param cacheType         Specific type of cache to be created.
   * @param loader            cache loader function.
   * @return a new cache.
   */
  private <V, C extends RedisCacheV2<String, V>> C getCacheList(String name, int expireTime, int logicalExpireTime, Class<V> valueClass, Class<C> cacheType,
      FailableFunction<String, List<V>, Exception> loader) {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("Cache name cannot be empty");
    }
    if (!caches.contains(name)) {
      try {
        C cache = cacheType.getDeclaredConstructor(RedisCommands.class, String.class, int.class, int.class, Class.class, Function.class, FailableFunction.class)
            .newInstance(redis, name, expireTime, logicalExpireTime, valueClass, Function.identity(), loader);
        caches.add(name);
        return cache;
      } catch (Exception e) {
        throw new CacheInitializationException("There was a problem when initializing cache with name: " + name, e);
      }
    }
    throw new InvalidParameterException("Cache with name " + name + " already exist");
  }

  /** Closes the connection to Redis and shutdown the client. */
  public void destroy() {
    this.connection.close();
    this.client.shutdown();
  }
}
