package com.ensolvers.fox.cache.redis;

import java.util.function.Function;

@FunctionalInterface
public interface CheckedFunction<T, R> extends Function<T, R> {
  @Override
  default R apply(T t) {
    try {
      return applyThrows(t);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  R applyThrows(T elem) throws Exception;
}
