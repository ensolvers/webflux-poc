package com.ensolvers.fox.cache.exception;

public abstract class CacheException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  protected CacheException() {
  }

  protected CacheException(String message) {
    super(message);
  }

  protected CacheException(String message, Throwable cause) {
    super(message, cause);
  }
}
