package com.ensolvers.fox.cache.spring.providers;

import com.ensolvers.fox.cache.common.CacheString;
import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.cache.spring.key.CustomCacheKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.spy.memcached.MemcachedClient;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Memcached implementation. Instances of this class must be passed to the cache manager.
 */
public class SpringMemcachedCache implements Cache {
  private final String name;
  private final MemcachedClient memcachedClient;
  private final ObjectMapper objectMapper;
  private final boolean allowNullValues;
  private final int expirationTimeInSeconds;

  /**
   * @param name                    an identifier for the cache
   * @param memcachedClient         the client to do the request to memcached service
   * @param expirationTimeInSeconds expiration time of the entries saved in memcached
   * @param allowNullValues         if null values are allowed (if true throws a {@link CacheInvalidArgumentException}
   *                                when a null value is detected)
   */
  public SpringMemcachedCache(String name, MemcachedClient memcachedClient, int expirationTimeInSeconds, boolean allowNullValues) {
    this.name = name;
    this.memcachedClient = memcachedClient;
    this.objectMapper = new ObjectMapper();
    this.allowNullValues = allowNullValues;
    this.expirationTimeInSeconds = expirationTimeInSeconds;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Object getNativeCache() {
    return memcachedClient;
  }

  /**
   * Get operation. Because spring do not provide support for bulk operations we distinguish it checking the return type
   * and the amount and type of parameters For bulk request: return type must be a Map and only one parameter of type
   * Collection must be provided (check {@link CustomCacheKey#isBulk()})
   * 
   * @param key The type of this parameter can be a {@link CustomCacheKey}, List or a "simple" type (String, Integer, etc)
   * @return null if no hit, ValueWrapper otherwise
   */
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

  /**
   * Put operation Because spring do not provide support for bulk operations we distinguish it checking the return type
   * and the amount and type of parameters For bulk request: return type must be a Map and only one parameter of type
   * Collection must be provided (check {@link CustomCacheKey#isBulk()})
   * 
   * @param key   The type of this parameter can be a {@link CustomCacheKey}, List or a "simple" type (String, Integer,
   *              etc)
   * @param value value to store in the cache
   */
  @Override
  public void put(Object key, Object value) {
    // Check if is a bulk put or not
    if (CustomCacheKey.class.isInstance(key) && ((CustomCacheKey) key).isBulk()) {
      // value to store must be an instance of Map (key with his value)
      if (!(value instanceof Map)) {
        throw new CacheInvalidArgumentException("Expected an instance of Map class in param type");
      }

      for (String k : (Collection<? extends String>) ((CustomCacheKey) key).getParams()[0]) {
        putSingle(k, ((Map<?, ?>) value).get(k));
      }
    } else {
      putSingle(key, value);
    }
  }

  /**
   * Delete operation. Remove a specific key from the cache
   * 
   * @param key the key to remove
   */
  @Override
  public void evict(Object key) {
    String cacheKey = getCacheKey(key);
    memcachedClient.delete(cacheKey);
  }

  /**
   * Delete all entries in the cache Note that this will delete all entries in memcached service (global)
   */
  @Override
  public void clear() {
    memcachedClient.flush();
  }

  /**
   * Build the final key to use in the cache
   * 
   * @param key the key of the object
   * @return the final key (a string conformed with the name of the cache and the params of the method)
   */
  private String getCacheKey(Object key) {
    StringBuilder cacheKeyBuilder = new StringBuilder();
    cacheKeyBuilder.append(name);

    if (key instanceof CustomCacheKey) {
      if (((CustomCacheKey) key).isEmpty()) {
        cacheKeyBuilder.append("-").append("UNIQUE");
      } else {
        cacheKeyBuilder.append("-").append(key);
      }
    } else if (key instanceof Collection) {
      ((Collection) key).forEach(o -> cacheKeyBuilder.append("-").append(o));
    } else {
      cacheKeyBuilder.append("-").append(key);
    }

    return cacheKeyBuilder.toString().replace(" ", "-");
  }

  private Object deserializeUsingMapArgumentType(CustomCacheKey customCacheKey, String cacheKey, String hit) {
    // Get the type of the serialized object
    Type type = ((ParameterizedType) customCacheKey.getMethod().getGenericReturnType()).getActualTypeArguments()[1];
    try {
      return objectMapper.readValue(hit, objectMapper.getTypeFactory().constructType(type));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(cacheKey, type.getTypeName(), e);
    }
  }

  private Object deserializeUsingReturnType(CustomCacheKey customCacheKey, String cacheKey, String hit) {
    // Get the type of the serialized object
    Type type = customCacheKey.getMethod().getGenericReturnType();
    try {
      return objectMapper.readValue(hit, objectMapper.getTypeFactory().constructType(type));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(cacheKey, type.getTypeName(), e);
    }
  }

  private void putSingle(Object key, Object value) {
    // Check null value
    if ((!allowNullValues) && value == null) {
      throw new CacheInvalidArgumentException("Cache '" + name + "' is configured to not allow null values but null was provided");
    }

    String cacheKey = getCacheKey(key);
    try {
      String serializedValue = CacheString.NULL_STRING;
      if (value != null) {
        serializedValue = objectMapper.writeValueAsString(value);
      }
      memcachedClient.set(cacheKey, expirationTimeInSeconds, serializedValue);
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(cacheKey, value.getClass(), e);
    }
  }

  private ValueWrapper getSingle(Object key) {
    // Get cached object
    String cacheKey = getCacheKey(key);
    String hit = (String) memcachedClient.get(cacheKey);

    // Missed hit
    if (hit == null) {
      return null;
    }

    if (hit.equals(CacheString.NULL_STRING)) {
      return new SimpleValueWrapper(null);
    }

    Object deserializedObject = deserializeUsingReturnType((CustomCacheKey) key, cacheKey, hit);
    return new SimpleValueWrapper(deserializedObject);
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
    Map<String, Object> hits = memcachedClient.getBulk(cacheKeyToOriginalKey.keySet());

    // Deserialize cached objects
    hits.forEach((cacheKey, hit) -> {
      Object deserializedObject = null;
      if (!hit.equals(CacheString.NULL_STRING)) {
        deserializedObject = deserializeUsingMapArgumentType(customCacheKey, cacheKey, (String) hit);
      }
      result.put(cacheKeyToOriginalKey.get(cacheKey), deserializedObject);
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
