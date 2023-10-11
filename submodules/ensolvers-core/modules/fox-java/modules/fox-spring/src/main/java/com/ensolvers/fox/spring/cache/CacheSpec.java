package com.ensolvers.fox.spring.cache;

import org.springframework.cache.Cache;

public class CacheSpec {

  private String cacheName;
  private Cache cache;

  public CacheSpec(String cacheName, Cache cache) {
    this.cacheName = cacheName;
    this.cache = cache;
  }

  public String getCacheName() {
    return cacheName;
  }

  public void setCacheName(String cacheName) {
    this.cacheName = cacheName;
  }

  public Cache getCache() {
    return cache;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }
}
