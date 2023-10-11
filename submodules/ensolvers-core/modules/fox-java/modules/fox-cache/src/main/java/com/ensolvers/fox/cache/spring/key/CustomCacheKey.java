package com.ensolvers.fox.cache.spring.key;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * The instances of this class are created in {@link CustomKeyGenerator} The purpose of this class is to replace default
 * wrapper {@link org.springframework.cache.interceptor.SimpleKey} provided by spring and wrap all necessary data about
 * the annotated method invoked and his context. Spring passes the created instances of this class to the corresponding
 * cache (declared in the cache manager) which will use this data to build a key to retrieve or save the corresponding
 * data in the cache.
 */
public class CustomCacheKey implements Serializable {
  private final transient Object target;
  private final transient Method method;
  private final transient Object[] params;
  private transient int hashCode;

  public CustomCacheKey(Object target, Method method, Object... elements) {
    Assert.notNull(elements, "Elements must not be null");
    this.target = target;
    this.method = method;
    this.params = elements.clone();
    this.hashCode = Arrays.deepHashCode(this.params);
  }

  public boolean isBulk() {
    return params.length == 1 && params[0] instanceof Collection;
  }

  public Object[] getParams() {
    return params;
  }

  public boolean isEmpty() {
    return params.length == 0;
  }

  public boolean equals(@Nullable Object other) {
    return this == other || (other instanceof CustomCacheKey && Arrays.deepEquals(this.params, ((CustomCacheKey) other).params));
  }

  public final int hashCode() {
    return this.hashCode;
  }

  public String toString() {
    return StringUtils.arrayToDelimitedString(this.params, "-");
  }

  private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    this.hashCode = Arrays.deepHashCode(this.params);
  }

  public Method getMethod() {
    return method;
  }

  public Object getTarget() {
    return target;
  }
}
