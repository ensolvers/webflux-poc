package com.ensolvers.fox.services.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class KeyBasedExecutorTest {

  private Logger logger = LoggerFactory.getLogger(KeyBasedExecutorTest.class);

  @Test
  void runThreadBasedOnKey() throws InterruptedException {
    KeyBasedExecutor keyBasedExecutor = new KeyBasedExecutor();
    AtomicLong invocationCount = new AtomicLong();
    ExecutorService executorService = Executors.newFixedThreadPool(100);

    AtomicLong wrongValueCount = new AtomicLong();
    final int MAX_KEYS = 3;

    for (int i = 0; i < 1000000; i++) {
      int finalI = i;

      // we try to run a Runnable with a max amount of MAX_KEYS keys, trying to find
      // a moment in time in which more that more than MAX_KEYS were being executed at
      // the same time
      executorService.submit(() -> {
        keyBasedExecutor.runThreadBasedOnKey("key" + (finalI % MAX_KEYS), () -> {
          long value = invocationCount.incrementAndGet();
          logger.info("Value {}", "key" + (finalI % MAX_KEYS));

          if (value > MAX_KEYS) {
            // if this happens at this point, it indicated that in some point in time
            // more than MAX_KEYS threads will be running in parallel, which means that
            // at least one thread ran in parallel with another with the same key
            wrongValueCount.incrementAndGet();
          }

          invocationCount.decrementAndGet();
        });
      });
    }

    executorService.shutdown();
    assertTrue(executorService.awaitTermination(10, TimeUnit.SECONDS));

    assertEquals(0, wrongValueCount.get());
  }
}