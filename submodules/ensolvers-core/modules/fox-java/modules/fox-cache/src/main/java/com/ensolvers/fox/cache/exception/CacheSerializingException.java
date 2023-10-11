package com.ensolvers.fox.cache.exception;

public class CacheSerializingException extends CacheException {
  public CacheSerializingException(String message, Throwable cause) {
    super(message, cause);
  }

  public static CacheSerializingException with(String key, Class<?> type, Throwable e) {
    return with(key, type.getName(), e);
  }

  public static CacheSerializingException with(String key, String type, Throwable e) {
    return new CacheSerializingException("Error when trying to deserialize object with " + "key: [" + key + "], " + "type: [" + type + "]", e);
  }

  public static CacheSerializingException with(String key, String type, String content, Throwable e) {
    return new CacheSerializingException(
        "Error when trying to deserialize object with " + "key: [" + key + "], " + "type: [" + type + "], " + "content: [" + content + "]", e);
  }
}
