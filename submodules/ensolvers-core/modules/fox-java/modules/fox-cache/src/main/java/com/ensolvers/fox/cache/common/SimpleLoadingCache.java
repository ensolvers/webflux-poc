package com.ensolvers.fox.cache.common;

import java.util.function.Consumer;

public interface SimpleLoadingCache<V> {
  V getUnchecked(String key);

  void invalidate(String key);

  void addInvalidationListener(Consumer<String> listener);
}
