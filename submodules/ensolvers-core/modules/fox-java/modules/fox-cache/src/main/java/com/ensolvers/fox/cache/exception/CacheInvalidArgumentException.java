package com.ensolvers.fox.cache.exception;

public class CacheInvalidArgumentException extends CacheException {
  public CacheInvalidArgumentException(String message) {
    super(message);
  }

  public CacheInvalidArgumentException(String message, Throwable cause) {
    super(message, cause);
  }

  public static CacheInvalidArgumentException collectionError(Class<?> collectionClass, Throwable e) {
    return new CacheInvalidArgumentException("Error trying to generate a copy of the passed collection. Invalid collection type " + collectionClass.getName(),
        e);
  }
}
