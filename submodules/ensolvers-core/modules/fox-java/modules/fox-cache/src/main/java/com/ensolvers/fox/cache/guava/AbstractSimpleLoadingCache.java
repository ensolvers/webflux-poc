package com.ensolvers.fox.cache.guava;

import com.ensolvers.fox.cache.common.SimpleLoadingCache;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class AbstractSimpleLoadingCache<V> implements SimpleLoadingCache<V> {
  protected final ArrayList<Consumer<String>> invalidationListeners;

  public AbstractSimpleLoadingCache() {
    this.invalidationListeners = new ArrayList<Consumer<String>>();
  }

  @Override
  public abstract V getUnchecked(String key);

  @Override
  public final void invalidate(String key) {
    this.invalidateEntry(key);
    this.notifyAllInvalidationListeners(key);
  }

  protected abstract void invalidateEntry(String key);

  protected void notifyAllInvalidationListeners(String key) {
    this.invalidationListeners.forEach(listener -> listener.accept(key));
  }

  @Override
  public void addInvalidationListener(Consumer<String> listener) {
    this.invalidationListeners.add(listener);
  }
}
