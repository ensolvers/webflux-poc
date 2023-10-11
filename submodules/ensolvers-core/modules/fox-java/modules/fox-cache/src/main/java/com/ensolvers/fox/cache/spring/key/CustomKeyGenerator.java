package com.ensolvers.fox.cache.spring.key;

import org.springframework.cache.interceptor.KeyGenerator;
import java.lang.reflect.Method;

/**
 * The purpose of this class is to replace default key generator provided by spring:
 * {@link org.springframework.cache.interceptor.SimpleKeyGenerator} This class creates an instance of
 * {@link CustomCacheKey} that will be passed by spring to the corresponding cache declared in the cache manager
 */
public class CustomKeyGenerator implements KeyGenerator {
  @Override
  public Object generate(Object target, Method method, Object... params) {
    return new CustomCacheKey(target, method, params);
  }
}
