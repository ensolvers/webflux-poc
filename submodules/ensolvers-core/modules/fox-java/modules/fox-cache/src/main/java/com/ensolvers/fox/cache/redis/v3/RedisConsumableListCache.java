package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.util.JSONSerializer;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RList;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class works as a cache of a list of items that we want to consume. After providing a loader
 * function that is invoked when the cache is empty for a particular key, the cache is hydrated with
 * the result of a loader and then the {@link RedisConsumableListCache#getFromList(String, int)} method can
 * be used to obtain the next set of n elements in a concurrent-safe way - that means, we ensure that we give a
 * specific sublist of elements to each thread/node
 */
public class RedisConsumableListCache<V> {
  private final Class<V> objectType;
  private final Function<String, List<V>> provider;
  private final int expirationInSeconds;
  private final RedissonClient client;

  private static final Logger logger = CoreLogger.getLogger(RedisConsumableListCache.class);

  public RedisConsumableListCache(String redisURI, Class<V> objectType, Function<String, List<V>> provider, int expirationInSeconds) {
    Config config = new Config();
    config.useSingleServer().setAddress(redisURI);
    this.client = Redisson.create(config);
    this.objectType = objectType;
    this.provider = provider;
    this.expirationInSeconds = expirationInSeconds;
  }

  private String getListName(String listKey) {
    return RedisConsumableListCache.class.getName() + "-List-" + listKey;
  }

  private String getListIndexName(String listKey) {
    return RedisConsumableListCache.class.getName() + "-Index-" + listKey;
  }

  private String getListLoadingSemaphoreName(String listKey) {
    return RedisConsumableListCache.class.getName() + "-Semaphore-" + listKey;
  }

  private RList<V> getList(String listKey, boolean autoInvalidate) throws Exception {
    RList<V> list = this.client.getList(getListName(listKey));

    // if the list does not exist...
    if (!list.isExists() || autoInvalidate) {
      logger.info("List [{}] does not exists, trying to grab lock for loading", listKey);

      // first we ensure that only one thread/node can fetch the list through a semaphore
      RSemaphore semaphore = this.client.getSemaphore(getListLoadingSemaphoreName(listKey));
      semaphore.trySetPermits(1);
      semaphore.acquire();

      try {
        // now, at this point only one thread/node must be executing this, we ensure
        // that the list was still not loaded by other thread/node
        list = this.client.getList(getListName(listKey));

        // TODO: Review this
        boolean hasEntries = this.getCurrentIndexFor(listKey).get() + 1 < list.size();

        // if still not, we proceed to loading
        if (!list.isExists() || (autoInvalidate && !hasEntries)) {
          logger.info("Loading content for [{}]", listKey);

          loadList(listKey, list);
        }
      } finally {
        semaphore.release();
      }
    }

    return list;
  }

  private RList<V> getList(String listKey) throws Exception {
    return this.getList(listKey, false);
  }

  private void loadList(String listKey, RList<V> list) throws Exception {
    list.clear();

    // we create a list with a least one dummy element to avoid Redisson returning isExists() even if the
    // entry for the list exists. We start with index = 1 because of this.
    List<V> freshList = this.provider.apply(listKey);
    freshList.add(0, this.objectType.getConstructor().newInstance());

    List<V> items = JSONSerializer.get().serializeList(freshList);

    list.addAll(items);
    list.expire(Instant.now().plusSeconds(this.expirationInSeconds));
    this.getCurrentIndexFor(listKey).set(1);

    logger.info("Content loaded for [{}]", listKey);
  }

  public void invalidate(String listKey) {
    RList<V> list = this.client.getList(getListName(listKey));
    try {
      this.loadList(listKey, list);
    } catch (Exception e) {
      logger.info("Can't invalidate cache");
    }
  }

  /**
   * Get the next {@code count} elements from the list. An empty list is returned if there
   * are no elements left
   *
   * @param listKey the key of the list from which elements are going to be fetched
   * @param count the amount of elements to be fetched
   * @return the list of fetched elements
   *
   * @throws Exception
   */
  public List<V> getFromList(String listKey, int count, boolean autoInvalidate) throws Exception {
    // obtains an atomic long which represents the current index, adds the count
    // IMPORTANT: we get the list first, since the first time the atomic counter y set to 0
    RList<V> list = this.getList(listKey, autoInvalidate);
    RAtomicLong atomicLong = getCurrentIndexFor(listKey);
    long index = atomicLong.getAndAdd(count);

    if (index + 1 >= list.size()) {
      // if the index is already before the last element, we return an empty list
      return autoInvalidate ? List.of() : this.getFromList(listKey, count, true);
    }

    // get and return next objects
    List<String> objects = list.range((int) index, ((int) index) + count - 1).stream().map(Object::toString).collect(Collectors.toList());
    return JSONSerializer.get().deserializeList(objects, this.objectType);
  }

  public List<V> getFromList(String listKey, int count) throws Exception {
    return this.getFromList(listKey, count, false);
  }

  /**
   * Obtains the amount of items yet to be consumed in the list
   * @param listKey key for the list
   * @return the amount of items yet to be consumed in the list
   * @throws Exception
   */
  public int remainingItems(String listKey) throws Exception {
    return this.getList(listKey).size() - (int) this.getCurrentIndexFor(listKey).get();
  }

  private RAtomicLong getCurrentIndexFor(String listKey) {
    return this.client.getAtomicLong(getListIndexName(listKey));
  }
}
