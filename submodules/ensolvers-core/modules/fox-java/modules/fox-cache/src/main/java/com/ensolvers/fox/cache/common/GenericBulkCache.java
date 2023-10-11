package com.ensolvers.fox.cache.common;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface GenericBulkCache<T> extends GenericCache<T> {
  Map<String, T> getMap(Collection<String> keys);

  List<T> getList(Collection<String> keys);
}
