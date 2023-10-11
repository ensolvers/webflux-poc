package com.ensolvers.fox.services.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleRateLimiterTest {

  @Test
  void testThrottle() throws InterruptedException {
    SimpleRateLimiter simpleRateLimiter = new SimpleRateLimiter(4);

    AtomicInteger invocationCount = new AtomicInteger();

    // try to execute 100k method calls, by using 1k keys, only 1k executions should
    // be done
    for (int i = 0; i < 100000; i++) {
      simpleRateLimiter.throttle("key" + i % 1000, () -> invocationCount.getAndIncrement());
    }
    assertEquals(1000, invocationCount.get());

    // wait for 2 secs until that key expires
    Thread.sleep(8000);

    // now after trying another 100k times again with another 1k keys, only 2k
    // executions should be registered
    for (int i = 0; i < 100000; i++) {
      simpleRateLimiter.throttle("key" + i % 1000, () -> invocationCount.getAndIncrement());
    }
    assertEquals(2000, invocationCount.get());
  }
}