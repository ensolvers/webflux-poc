package com.ensolvers.fox.services.util;

import com.ensolvers.fox.services.logging.CoreLogger;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleRateLimiter {

  private static final Logger logger = CoreLogger.getLogger(SimpleRateLimiter.class);

  private final Set<Object> keySet;
  private final int secondsToWait;
  private final ScheduledExecutorService scheduler;

  public SimpleRateLimiter(int secondsToWait) {
    this.keySet = ConcurrentHashMap.newKeySet();
    this.secondsToWait = secondsToWait;
    this.scheduler = Executors.newScheduledThreadPool(1);
  }

  /**
   * Invokes a Runnable with a registered `key` only if a call with the same `key` was not registered before
   * 
   * @param key      key for identifying the Runnable unequivocally
   * @param runnable runnable to be called
   */
  public void throttle(Object key, Runnable runnable) {
    if (keySet.contains(key)) {
      logger.info(key + " invocation limited");
      return;
    }

    // limit bypassed, invoking the runnable
    keySet.add(key);
    runnable.run();

    // schedule key cleanup after
    this.scheduler.schedule(() -> keySet.remove(key), this.secondsToWait, TimeUnit.SECONDS);
  }
}
