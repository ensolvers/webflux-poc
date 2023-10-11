package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.common.CacheString;
import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.support.SimpleValueWrapper;

/**
 * Spring Cache compatible implementation using Guava as ahe underlying in-memory cache
 */
public class SpringGuavaCache implements org.springframework.cache.Cache {
  private final String name;
  private final Cache<String, Object> guavaCache;
  private final boolean allowNullValues;

  public SpringGuavaCache(String name, long expirationTimeInSeconds, boolean allowNullValues) {
    this.name = name;
    this.guavaCache = CacheBuilder.newBuilder().expireAfterWrite(expirationTimeInSeconds, TimeUnit.SECONDS).build();
    this.allowNullValues = allowNullValues;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return guavaCache;
  }

  @Override
  public ValueWrapper get(Object key) {
    // Check if is a bulk get or not
    if (CustomCacheKey.class.isInstance(key) && ((CustomCacheKey) key).isBulk()) {
      return getBulk((CustomCacheKey) key);
    } else {
      return getSingle(key);
    }
  }

  @Override
  public <T> T get(Object key, Class<T> aClass) {
    ValueWrapper wrapper = this.get(key);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public <T> T get(Object key, Callable<T> callable) {
    ValueWrapper wrapper = this.get(key);
    return wrapper == null ? null : (T) wrapper.get();
  }

  @Override
  public void put(Object key, Object value) {
    if (value == null) {
      if (!allowNullValues) {
        throw new CacheInvalidArgumentException("Cache '" + name + "' is configured to not allow null values but null was provided");
      } else {
        guavaCache.put(getCacheKey(key), CacheString.NULL_STRING);
        return;
      }
    }

    guavaCache.put(getCacheKey(key), value);
  }

  @Override
  public void evict(Object key) {
    guavaCache.invalidate(getCacheKey(key));
  }

  @Override
  public void clear() {
    guavaCache.invalidateAll();
  }

  /**
   * Build the final key to use in the cache
   * 
   * @param key the key of the object
   * @return the final key (a string conformed with the name of the cache and the params of the method)
   */
  private String getCacheKey(Object key) {
    StringBuilder finalKeyBuilder = new StringBuilder();
    finalKeyBuilder.append(name);

    if (key instanceof CustomCacheKey) {
      if (((CustomCacheKey) key).isEmpty()) {
        finalKeyBuilder.append("-").append("UNIQUE");
      } else {
        finalKeyBuilder.append("-").append(key);
      }
    } else if (key instanceof Collection) {
      ((Collection) key).forEach(o -> finalKeyBuilder.append("-").append(o));
    } else {
      finalKeyBuilder.append("-").append(key);
    }

    return finalKeyBuilder.toString().replace(" ", "-");
  }

  private void putSingle(Object key, Object value) {
    // Check null value
    if ((!allowNullValues) && value == null) {
      throw new CacheInvalidArgumentException("Cache '" + name + "' is configured to not allow null values but null was provided");
    }

    guavaCache.put(getCacheKey(key), value == null ? CacheString.NULL_STRING : value);
  }

  private ValueWrapper getSingle(Object key) {
    // Get cached object
    Object result = this.guavaCache.asMap().get(getCacheKey(key));

    if (result == null) {
      return null;
    }

    if (result.equals(CacheString.NULL_STRING)) {
      return new SimpleValueWrapper(null);
    }

    return new SimpleValueWrapper(result);
  }

  private ValueWrapper getBulk(CustomCacheKey customCacheKey) {
    // Check that return type is subclass of Map
    if (!Map.class.isAssignableFrom(customCacheKey.getMethod().getReturnType())) {
      throw new CacheInvalidArgumentException("Expected an instance of Map class in return type");
    }

    // Get the collection of requested keys
    Collection<Object> collection = (Collection<Object>) customCacheKey.getParams()[0];

    // Convert key to cache key
    Map<String, Object> cacheKeyToOriginalKey = collection.stream().collect(Collectors.toMap(this::getCacheKey, Function.identity(), (v1, v2) -> v1));
    Map<Object, Object> result = new HashMap<>();

    // Get cached objects
    Map<String, Object> hits = new HashMap<>();
    cacheKeyToOriginalKey.keySet().forEach(cacheKey -> {
      Object hit = guavaCache.asMap().get(cacheKey);
      if (hit != null) {
        hits.put(cacheKey, hit);
      }
    });

    // Deserialize cached objects
    hits.forEach((cacheKey, hit) -> {
      result.put(cacheKeyToOriginalKey.get(cacheKey), hit == CacheString.NULL_STRING ? null : hit);
      cacheKeyToOriginalKey.remove(cacheKey);
    });

    // Check missed hits
    if (!cacheKeyToOriginalKey.isEmpty()) {
      // Create a new instance of the collection class and collect the missed keys to
      // pass it to the annotated method
      Collection missedKeys;
      try {
        missedKeys = (Collection) customCacheKey.getParams()[0].getClass().getDeclaredConstructor().newInstance();
        missedKeys.addAll(cacheKeyToOriginalKey.values());
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw CacheInvalidArgumentException.collectionError(customCacheKey.getParams()[0].getClass(), e);
      }

      // Execute the method to retrieve the missed hits
      Map missedHits;
      try {
        missedHits = (Map) customCacheKey.getMethod().invoke(customCacheKey.getTarget(), missedKeys);
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new CacheExecutionException("Error trying to execute annotated method. Check stack trace for more information.", e);
      }

      // Cache the missed hits and add to the result
      missedKeys.forEach(missedKey -> {
        this.putSingle(missedKey, missedHits.get(missedKey));
        result.put(missedKey, missedHits.get(missedKey));
      });
    }

    // Return the result
    return new SimpleValueWrapper(result);
  }
}
