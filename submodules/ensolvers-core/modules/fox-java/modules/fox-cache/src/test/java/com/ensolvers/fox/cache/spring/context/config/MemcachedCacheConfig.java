package com.ensolvers.fox.cache.spring.context.config;

import com.ensolvers.fox.cache.spring.GenericCacheManager;
import com.ensolvers.fox.cache.spring.key.CustomKeyGenerator;
import com.ensolvers.fox.cache.spring.providers.SpringMemcachedCache;
import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.InetSocketAddress;

@EnableCaching
public class MemcachedCacheConfig extends CachingConfigurerSupport {
  @Value("${cache.memcache.port}")
  private String memcachedPort;

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new CustomKeyGenerator();
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    MemcachedClient client;
    try {
      client = new MemcachedClient(new InetSocketAddress(Integer.parseInt(memcachedPort)));
    } catch (IOException e) {
      throw new RuntimeException("Error trying to instantiate memcached bean", e);
    }

    SpringMemcachedCache testCache = new SpringMemcachedCache("test", client, 60000, false);
    SpringMemcachedCache profileCache = new SpringMemcachedCache("profile", client, 60000, false);
    SpringMemcachedCache profileCacheNullable = new SpringMemcachedCache("profileNullable", client, 60000, true);

    return new GenericCacheManager().append("test", testCache).append("profile", profileCache).append("profileNullable", profileCacheNullable);
  }
}
