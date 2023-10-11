package com.ensolvers.fox.cache.spring.context.config;

import com.ensolvers.fox.cache.spring.GenericCacheManager;
import com.ensolvers.fox.cache.spring.key.CustomKeyGenerator;
import com.ensolvers.fox.cache.spring.providers.SpringMemcachedCache;
import com.ensolvers.fox.cache.spring.providers.SpringRedisCache;
import io.lettuce.core.RedisClient;
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
public class RedisCacheConfig extends CachingConfigurerSupport {
  @Value("${cache.redis.port}")
  private String redisPort;

  @Bean
  @Override
  public KeyGenerator keyGenerator() {
    return new CustomKeyGenerator();
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    var client = RedisClient.create("redis://localhost:" + redisPort + "/0").connect().sync();

    SpringRedisCache testCache = new SpringRedisCache("test", client, 60000, false);
    SpringRedisCache profileCache = new SpringRedisCache("profile", client, 60000, false);
    SpringRedisCache profileCacheNullable = new SpringRedisCache("profileNullable", client, 60000, true);

    return new GenericCacheManager().append("test", testCache).append("profile", profileCache).append("profileNullable", profileCacheNullable);
  }
}
