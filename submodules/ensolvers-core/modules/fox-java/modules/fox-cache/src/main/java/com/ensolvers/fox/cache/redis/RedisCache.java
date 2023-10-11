package com.ensolvers.fox.cache.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.api.sync.RedisCommands;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RedisCache<V> {
  Logger logger = LoggerFactory.getLogger(RedisCache.class);

  protected RedisCommands<String, String> redis;
  protected long expirationTime;
  protected String cacheName;
  protected static final int KEY_SCAN_MAX_LIMIT = 1000;
  protected static final String KEY_SEPARATOR = "::";
  /**
   * @deprecated Use valueType instead
   */
  @Deprecated(since = "2022-12-16")
  protected final Class<V> valueClass;
  protected final CheckedFunction<V, String> customSerializer;
  protected final CheckedFunction<String, V> customDeserializer;
  protected final JavaType valueType;
  protected final ObjectMapper objectMapper;
  protected final Integer maxEntriesPerBlock;

  public RedisCache(RedisCommands<String, String> redis, String cacheName, int expirationTime, Class<V> valueClass, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    this.redis = redis;
    this.cacheName = cacheName;
    this.expirationTime = expirationTime;
    this.valueClass = valueClass;
    this.customSerializer = customSerializer;
    this.customDeserializer = customDeserializer;
    this.objectMapper = new ObjectMapper();
    this.valueType = this.objectMapper.getTypeFactory().constructType(valueClass);
    this.maxEntriesPerBlock = maxEntriesPerBlock;
  }

  public RedisCache(RedisCommands<String, String> redis, String cacheName, int expirationTime, JavaType javaType, CheckedFunction<V, String> customSerializer,
      CheckedFunction<String, V> customDeserializer, Integer maxEntriesPerBlock) {
    this.redis = redis;
    this.cacheName = cacheName;
    this.expirationTime = expirationTime;
    this.valueClass = null;
    this.customSerializer = customSerializer;
    this.customDeserializer = customDeserializer;
    this.objectMapper = new ObjectMapper();
    this.valueType = javaType;
    this.maxEntriesPerBlock = maxEntriesPerBlock;
  }

  /**
   * Obtains the final key adding a prefix so a single Redis instance can be shared by several caches.
   *
   * @param key The Key to compute.
   * @return The computed key.
   */
  protected String computeKey(String key) {
    return cacheName + KEY_SEPARATOR + key;
  }

  /**
   * Serializes a value instance so it can be stored in the cache.
   *
   * @param value The value to be serialized.
   * @return The serialized value.
   */
  protected String serializeValue(V value) throws JsonProcessingException {
    if (customSerializer != null) {
      return this.customSerializer.apply(value);
    } else {
      return this.objectMapper.writeValueAsString(value);
    }
  }

  /**
   * Deserializes the value back to the original type using the given serializers.
   *
   * @param serializedValue The serialized Value.
   * @return The original Value.
   */
  protected V deserializeValue(String serializedValue) throws IOException {
    if (serializedValue == null) {
      return null;
    }
    if (customDeserializer != null) {
      return this.customDeserializer.apply(serializedValue);
    } else {
      return this.objectMapper.readValue(serializedValue, valueType);
    }
  }

  /**
   * Used to convert Collection<V> to String[] in order to pass it as parameter.
   *
   * @param values a collection of V.
   * @return an Array of String with every element of the collection
   */
  protected String[] collectionOfVToStringArray(Collection<V> values) throws JsonProcessingException {
    String[] valuesAsString = new String[values.size()];
    int i = 0;
    for (V value : values) {
      valuesAsString[i] = this.serializeValue(value);
      i++;
    }
    return valuesAsString;
  }

  /**
   * Removes the entry from the cache.
   *
   * @param key The key of the entry to remove.
   */
  public void invalidate(String key) {
    this.redis.del(this.computeKey(key));
  }

  /** Invalidates every entry of the cache. */
  public void invalidateAll() {
    KeyScanCursor<String> cursor;
    do {
      cursor = redis.scan(ScanArgs.Builder.limit(KEY_SCAN_MAX_LIMIT).match(this.cacheName + KEY_SEPARATOR + "*"));
      if (cursor.getKeys() != null && cursor.getKeys().size() > 0) {
        this.redis.del(cursor.getKeys().toArray(new String[0]));
      }
    } while (!cursor.isFinished() && cursor.getKeys() != null && cursor.getKeys().size() > 0);
  }

  /**
   * Determines whether the key exists on the cache.
   *
   * @param key
   * @return True if the key exists, False if it doesn't.
   */
  public boolean keyExists(String key) {
    return this.redis.exists(this.computeKey(key)) > 0;
  }

  /**
   * Resets the Time To Live for the entry associated with the given key.
   *
   * @param key
   */
  public void resetTTL(String key) {
    notNull(key);
    this.redis.expire(this.computeKey(key), expirationTime);
  }

  /**
   * Opens a Redis Transaction, and executes all com.ensolvers.core.cli.services.commands sent within the lambda parameter atomically.
   *
   * @param callable () -> { redis command; reds command; [...] return null; }
   */
  protected void redisTransaction(Callable<?> callable) {
    this.redis.multi();
    try {
      callable.call();
      this.redis.exec();
    } catch (Exception e) {
      logger.error("[REDIS_CACHE] There was an error when executing the transaction", e);
      this.redis.discard();
      throw new RuntimeException(e);
    }
  }

  // Validators - for internal use
  protected void notEmpty(Collection collection) {
    if (collection == null || collection.size() == 0) {
      throw new IllegalArgumentException("The validated collection is empty");
    }
  }

  protected void notNull(Object object) {
    if (object == null) {
      throw new IllegalArgumentException();
    }
  }
}
