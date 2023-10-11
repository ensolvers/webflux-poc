package com.ensolvers.fox.cache.spring.context.config;

import com.ensolvers.fox.cache.spring.GenericCacheManager;
import com.ensolvers.fox.cache.spring.key.CustomKeyGenerator;
import com.ensolvers.fox.cache.spring.providers.SpringGuavaCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;

@EnableCaching
public class GuavaCacheConfig extends CachingConfigurerSupport {
  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new CustomKeyGenerator();
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    SpringGuavaCache testCache = new SpringGuavaCache("test", 60000, false);
    SpringGuavaCache profileCache = new SpringGuavaCache("profile", 60000, false);
    SpringGuavaCache profileCacheNullable = new SpringGuavaCache("profileNullable", 60000, true);

    return new GenericCacheManager().append("test", testCache).append("profile", profileCache).append("profileNullable", profileCacheNullable);
  }
}
