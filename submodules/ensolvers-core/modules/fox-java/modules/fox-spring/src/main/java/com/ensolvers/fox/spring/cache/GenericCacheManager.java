package com.ensolvers.fox.spring.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GenericCacheManager implements CacheManager {

  private Map<String, Cache> cacheMap;

  public GenericCacheManager(CacheSpec... caches) {
    this.cacheMap = new ConcurrentHashMap<>(Arrays.stream(caches).collect(Collectors.toMap(CacheSpec::getCacheName, CacheSpec::getCache)));
  }

  @Override
  public Cache getCache(String name) {
    return this.cacheMap.get(name);
  }

  @Override
  public Collection<String> getCacheNames() {
    return cacheMap.keySet();
  }

}
