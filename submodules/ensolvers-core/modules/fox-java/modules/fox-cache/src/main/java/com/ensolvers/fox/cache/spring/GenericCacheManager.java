package com.ensolvers.fox.cache.spring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Generic implementation of a Spring Cache Manager that uses a ConcurrentHashMap to index caches by name
 */
public class GenericCacheManager implements CacheManager {

  private Map<String, Cache> cacheMap;

  public GenericCacheManager(CacheSpec... caches) {
    this.cacheMap = new ConcurrentHashMap<>(Arrays.stream(caches).collect(Collectors.toMap(CacheSpec::getCacheName, CacheSpec::getCache)));
  }

  public GenericCacheManager append(String key, Cache cache) {
    cacheMap.put(key, cache);
    return this;
  }

  @Override
  public Cache getCache(String name) {
    return cacheMap.get(name);
  }

  @Override
  public Collection<String> getCacheNames() {
    return cacheMap.keySet();
  }
}
