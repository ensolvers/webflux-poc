package com.ensolvers.fox.cache;

import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
public class GuavaBackedCacheTest {

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void testGetAndExpiration() throws InterruptedException {
    AtomicLong loadCount = new AtomicLong(0);

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    SimpleLoadingCache<String> stringCache = Caches.newGuavaBackedRedisCache(1, 1, uri, "stringCache", String.class, (key) -> {
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

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    SimpleLoadingCache<String> stringCache = Caches.newGuavaBackedRedisCache(10, 10, uri, "stringCache", String.class, (key) -> {
      // we count each load via an Atomic Long
      loadCount.incrementAndGet();
      return key + "-value";
    });

    // we try to load the same key with 100 threads in parallel
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    for (int i = 0; i < 1000000; i++) {
      executorService.submit(() -> {
        stringCache.getUnchecked("key");
      });
    }

    executorService.shutdown();
    assertTrue(executorService.awaitTermination(100, TimeUnit.SECONDS));

    // ... only 1 must succeed
    assertEquals(1, loadCount.get());
  }
}
