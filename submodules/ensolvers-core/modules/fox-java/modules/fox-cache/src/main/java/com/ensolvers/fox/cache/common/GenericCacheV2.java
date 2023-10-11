package com.ensolvers.fox.cache.common;

/**
 * Generic cache interface to abstract underlying implementation
 *
 * @param <K> type of the key stored in the cache
 * @param <V> type of elements stored in the cache
 */
public interface GenericCacheV2<K, V> {

  /**
   * Obtains a new element
   *
   * @param key returns an element from the given key
   * @return the cached element
   */
  V get(K key);

  /**
   * Removes the entry matching with {@code key} in the cache
   *
   * @param key
   */
  void invalidate(K key);

  /**
   * Stores an item in the cache
   *
   * @param key    the key under which the item will be stored
   * @param object the item to be stored
   */
  void put(K key, V object);
}
