package com.ensolvers.fox.services.util;

@FunctionalInterface
public interface FailableRunnable<E extends Exception> {
  void run() throws E;
}
