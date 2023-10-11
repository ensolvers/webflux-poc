package com.ensolvers.fox.cache.guava;

import com.ensolvers.fox.cache.Caches;
import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import com.ensolvers.fox.cache.redis.v3.RedisLoadingCache;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class GuavaLoadingCacheTest {

  @Test
  public void testGetAndExpiration() throws InterruptedException {
    AtomicLong loadCount = new AtomicLong();

    SimpleLoadingCache<String> stringCache = Caches.newGuavaLoadingCache(1, "guavaCache", (key) -> {
      // we count each load via an Atomic Long
      loadCount.incrementAndGet();
      return key + "-value";
    });

    // 10 get operations are tried...
    for (int i = 0; i < 10; i++) {
      stringCache.getUnchecked("key");
    }
    // ... only 1 must succeed
    assertEquals(1, loadCount.get());

    // then, we invalidate they key, we try to load the object under that key again, then a second loading attempt
    // must have been triggered
    stringCache.invalidate("key");
    stringCache.getUnchecked("key");
    assertEquals(2, loadCount.get());

    // finally, we sleep 2 seconds (with expiration time of 1 sec), when we try to load the same key again,
    // which must trigger a third load attempt due to key expiration
    Thread.sleep(2000);
    stringCache.getUnchecked("key");
    assertEquals(3, loadCount.get());
  }

  @Test
  public void testConcurrentLoading() throws InterruptedException {
    AtomicLong loadCount = new AtomicLong(0);

    SimpleLoadingCache<String> stringCache = Caches.newGuavaLoadingCache(10, "stringCache", (key) -> {
      // we count each load via an Atomic Long
      loadCount.incrementAndGet();
      return key + "-value";
    });

    // we try to load the same key with 100 threads in parallel
    ExecutorService executorService = Executors.newFixedThreadPool(1000);
    for (int i = 0; i < 1000000; i++) {
      executorService.submit(() -> {
        assertEquals("key-value", stringCache.getUnchecked("key"));
      });
    }

    executorService.shutdown();
    assertTrue(executorService.awaitTermination(100, TimeUnit.SECONDS));

    // ... only 1 must succeed
    assertEquals(1, loadCount.get());
  }

}