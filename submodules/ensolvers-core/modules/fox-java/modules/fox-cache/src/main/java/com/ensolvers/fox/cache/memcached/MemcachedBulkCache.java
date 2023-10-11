package com.ensolvers.fox.cache.memcached;

import com.ensolvers.fox.cache.common.CacheString;
import com.ensolvers.fox.cache.common.GenericBulkCache;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import net.spy.memcached.MemcachedClient;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MemcachedBulkCache<T> extends MemcachedCache<T> implements GenericBulkCache<T> {
  private final Function<Collection<String>, Map<String, T>> fetchMultiFunction;

  protected MemcachedBulkCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction,
      Function<Collection<String>, Map<String, T>> fetchMultiFunction, String keyPrefix, Function<TypeFactory, JavaType> objectTypeFactory,
      int expirationTimeInSeconds, boolean allowNullValues, Function<T, String> customSerializer, Function<String, T> customDeserializer) {
    super(memcachedClient, fetchFunction, keyPrefix, objectTypeFactory, expirationTimeInSeconds, allowNullValues, customSerializer, customDeserializer);
    this.fetchMultiFunction = fetchMultiFunction;
  }

  public MemcachedBulkCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, Function<Collection<String>, Map<String, T>> fetchMultiFunction,
      String keyPrefix, Class<T> objectClass, int expirationTimeInSeconds, boolean allowNullValues, Function<T, String> customSerializer,
      Function<String, T> customDeserializer) {
    this(memcachedClient, fetchFunction, fetchMultiFunction, keyPrefix, f -> f.constructType(objectClass), expirationTimeInSeconds, allowNullValues,
        customSerializer, customDeserializer);
  }

  public MemcachedBulkCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, Function<Collection<String>, Map<String, T>> fetchMultiFunction,
      String keyPrefix, Function<TypeFactory, JavaType> objectTypeFactory, int expirationTimeInSeconds, boolean allowNullValues) {
    this(memcachedClient, fetchFunction, fetchMultiFunction, keyPrefix, objectTypeFactory, expirationTimeInSeconds, allowNullValues, null, null);
  }

  public MemcachedBulkCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, Function<Collection<String>, Map<String, T>> fetchMultiFunction,
      String keyPrefix, Class<T> objectClass, int expirationTimeInSeconds, boolean allowNullValues) {
    this(memcachedClient, fetchFunction, fetchMultiFunction, keyPrefix, f -> f.constructType(objectClass), expirationTimeInSeconds, allowNullValues, null,
        null);
  }

  @Override
  /**
   * Uses the fetch lambda Functions
   *
   * @param keys     a collection of keys
   * @param keyGroup the group of the key
   * @return the object
   */
  public Map<String, T> getMap(Collection<String> keys) {
    // Filter duplicated keys
    Set<String> keySet = new HashSet<>(keys);

    // Computation of keys keeping the correspondence with the original version
    Map<String, String> cacheKeyToOriginalKey = keySet.stream().collect(Collectors.toMap(this::computeKey, Function.identity()));

    // Get cached objects: computedKey -> object (String)
    Map<String, Object> hits = this.memcachedClient.getBulk(cacheKeyToOriginalKey.keySet());

    // Map to originalKey -> object (T)
    Map<String, T> objects = new HashMap<>(keys.size());

    // Convert hits to objects (T)
    hits.forEach((cacheKey, value) -> {
      try {
        if (!value.equals(CacheString.NULL_STRING)) {
          objects.put(cacheKeyToOriginalKey.get(cacheKey), this.convertToObject((String) value));
        }

        // Remove the hit
        cacheKeyToOriginalKey.remove(cacheKey);
      } catch (IOException e) {
        throw CacheSerializingException.with(keyPrefix, objectType.getTypeName(), (String) value, e);
      }
    });

    // Check hits missed
    if (!cacheKeyToOriginalKey.isEmpty()) {
      logger.info("Cache missed for {} objects for class {}", cacheKeyToOriginalKey.size(), this.objectType.getTypeName());

      // cache miss, go get the object
      Map<String, T> freshObjects = fetchMultiFunction.apply(cacheKeyToOriginalKey.values());

      // Save the fresh objects to the cache
      cacheKeyToOriginalKey.values().forEach(originalMissedKey -> {
        this.put(originalMissedKey, freshObjects.get(originalMissedKey));
      });

      // Add fresh objects to the result
      objects.putAll(freshObjects);
    }

    return objects;
  }

  @Override
  public List<T> getList(Collection<String> keys) {
    Map<String, T> resultMap = getMap(keys);
    return keys.stream().map(resultMap::get).filter(Objects::nonNull).collect(Collectors.toList());
  }
}
