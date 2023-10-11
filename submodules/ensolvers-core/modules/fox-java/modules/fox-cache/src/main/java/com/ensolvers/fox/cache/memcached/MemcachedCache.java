/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.cache.memcached;

import com.ensolvers.fox.cache.common.CacheString;
import com.ensolvers.fox.cache.common.GenericCache;
import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.function.Function;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple memcached-based cache
 *
 * @param <T> Type of objects that will be stored in the cache
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
public class MemcachedCache<T> implements GenericCache<T> {
  Logger logger = LoggerFactory.getLogger(MemcachedCache.class);

  protected final MemcachedClient memcachedClient;
  protected final Function<String, T> fetchFunction;
  protected final String keyPrefix;
  protected final JavaType objectType;
  protected final int expirationTimeInSeconds;
  private final boolean allowNullValues;

  private final ObjectMapper objectMapper;
  private Function<T, String> customSerializer;
  private Function<String, T> customDeserializer;

  public MemcachedCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, String keyPrefix, Function<TypeFactory, JavaType> objectTypeFactory,
      int expirationTimeInSeconds, boolean allowNullValues, Function<T, String> customSerializer, Function<String, T> customDeserializer) {
    this.memcachedClient = memcachedClient;
    this.fetchFunction = fetchFunction;
    this.keyPrefix = keyPrefix;
    this.objectMapper = new ObjectMapper();
    this.objectType = objectTypeFactory.apply(this.objectMapper.getTypeFactory());
    this.expirationTimeInSeconds = expirationTimeInSeconds;
    this.allowNullValues = allowNullValues;
    this.customSerializer = customSerializer;
    this.customDeserializer = customDeserializer;
  }

  /**
   * Adds custom serializer/deserializer
   *
   * @param memcachedClient         the memcached client used to store the objects
   * @param fetchFunction           the function to fetch the underlying object if not found in the cache
   * @param keyPrefix               the prefix that will be used to create the keys - since several caches can use the
   *                                same memcached instance, it is important that every one has its own prefix to avoid
   *                                collisions
   * @param objectClass             type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   * @param customSerializer        serializer that will be use
   * @param customDeserializer      custom deserializer.
   */
  public MemcachedCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, String keyPrefix, Class<T> objectClass, int expirationTimeInSeconds,
      boolean allowNullValues, Function<T, String> customSerializer, Function<String, T> customDeserializer) {

    this(memcachedClient, fetchFunction, keyPrefix, f -> f.constructType(objectClass), expirationTimeInSeconds, allowNullValues, customSerializer,
        customDeserializer);
  }

  /**
   * Creates a cache instance that allows to store single objects
   *
   * @param memcachedClient         the memcached client used to store the objects
   * @param fetchFunction           the function to fetch the underlying object if not found in the cache
   * @param keyPrefix               the prefix that will be used to create the keys - since several caches can use the
   *                                same memcached instance, it is important that every one has its own prefix to avoid
   *                                collisions
   * @param objectTypeFactory       when using the (default) Jackson serializer and complex objects (for instance, those
   *                                that have parametric types like Lists) needed to be stored in the cache, given a
   *                                Jackson TypeFactory, this function should return the final type
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, String keyPrefix, Function<TypeFactory, JavaType> objectTypeFactory,
      int expirationTimeInSeconds, boolean allowNullValues) {
    this(memcachedClient, fetchFunction, keyPrefix, objectTypeFactory, expirationTimeInSeconds, allowNullValues, null, null);
  }

  /**
   * Creates a cache instance that allows to store single objects
   *
   * @param memcachedClient         the memcached client used to store the objects
   * @param fetchFunction           the function to fetch the underlying object if not found in the cache
   * @param keyPrefix               the prefix that will be used to create the keys - since several caches can use the
   *                                same memcached instance, it is important that every one has its own prefix to avoid
   *                                collisions
   * @param objectClass             type of objects that will be stored in the cache
   * @param expirationTimeInSeconds the item expiration time in seconds
   */
  public MemcachedCache(MemcachedClient memcachedClient, Function<String, T> fetchFunction, String keyPrefix, Class<T> objectClass, int expirationTimeInSeconds,
      boolean allowNullValues) {
    this(memcachedClient, fetchFunction, keyPrefix, f -> f.constructType(objectClass), expirationTimeInSeconds, allowNullValues, null, null);
  }

  /**
   * Uses the fetch lambda Function
   *
   * @param key The key of the object
   * @return the object
   */
  @Override
  public T get(String key) {
    String computedKey = this.computeKey(key);
    String serializedObject = (String) this.memcachedClient.get(computedKey);

    // return the object
    if (serializedObject != null) {
      try {
        return this.convertToObject(serializedObject);
      } catch (IOException e) {
        throw CacheSerializingException.with(keyPrefix, objectType.getTypeName(), serializedObject, e);
      }
    }

    // cache miss, go get the object
    T freshObject = fetchFunction.apply(key);

    this.put(key, freshObject);

    return freshObject;
  }

  /**
   * Method for add new object (distinct from refresh) to the cache
   *
   * @param key         The key of the object
   * @param freshObject the object
   * @return the object
   */
  @Override
  public void put(String key, T freshObject) {
    // Check null value
    if ((!allowNullValues) && freshObject == null) {
      throw new CacheInvalidArgumentException("Cache with prefix '" + keyPrefix + "' is configured to not allow null values but null was provided");
    }

    try {
      this.memcachedClient.set(this.computeKey(key), this.expirationTimeInSeconds, this.convertToString(freshObject));
    } catch (JsonProcessingException e) {
      throw CacheSerializingException.with(this.computeKey(key), freshObject.getClass(), e);
    }
  }

  @Override
  public void invalidate(String key) {
    String finalKey = this.computeKey(key);
    this.memcachedClient.delete(finalKey);
  }

  protected String convertToString(T object) throws JsonProcessingException {
    if (object == null) {
      return CacheString.NULL_STRING;
    }

    // If custom serializer has been provided, use it...
    if (this.customSerializer != null) {
      return this.customSerializer.apply(object);
    }

    // ... otherwise, use Jackson
    return this.objectMapper.writeValueAsString(object);
  }

  protected T convertToObject(String serializedObject) throws IOException {
    if (serializedObject.equals(CacheString.NULL_STRING)) {
      return null;
    }

    // If custom deserializer has been provided, use it...
    if (this.customDeserializer != null) {
      return this.customDeserializer.apply(serializedObject);
    }

    // ... otherwise, use Jackson
    return this.objectMapper.readValue(serializedObject, this.objectType);
  }

  protected String computeKey(String key) {
    return this.keyPrefix + "-" + key;
  }
}
