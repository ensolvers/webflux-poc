package com.ensolvers.fox.cache.redis;

import java.util.Collection;

public interface RedisCollection<V> {

  /**
   * Given a key returns all the elements from a collection
   *
   * @param key The key of the collection.
   * @return A collection with all the elements. IMPORTANT: If the key does not exists this method will return an empty
   *         collection.
   */
  Collection<V> get(String key);

  /**
   * Removes all occurrences of an object from a collection.
   *
   * @param key   The key of the collection.
   * @param value The value to delete.
   */
  void del(String key, V value);

  /**
   * Removed all the occurrences of all the elements passed as parameter from a collection.
   *
   * @param key    The key of the collection.
   * @param values The values to delete.
   */
  void del(String key, Collection<V> values);

  /**
   * Prepends a value to a collection.
   *
   * @param key   The key of the collection.
   * @param value to be added
   */
  void push(String key, V value);

  /**
   * Prepend multiple values to a collection.
   *
   * @param key    The key of the collection.
   * @param values to be added.
   */
  void push(String key, Collection<V> values);

  /**
   * Returns the number of elements in a collection
   *
   * @param key The key of the collection.
   * @return N° of elements in the collection.
   */
  Long size(String key);
}
