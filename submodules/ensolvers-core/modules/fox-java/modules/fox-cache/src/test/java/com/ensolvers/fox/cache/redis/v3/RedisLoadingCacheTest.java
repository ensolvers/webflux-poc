package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.cache.Caches;
import com.ensolvers.fox.cache.common.SimpleLoadingCache;
import com.ensolvers.fox.services.logging.CoreLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisLoadingCacheTest {

  private static final Logger logger = CoreLogger.getLogger(RedisConsumableListCacheTest.class);

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void testGetAndExpiration() throws InterruptedException {
    AtomicLong loadCount = new AtomicLong(0);

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    SimpleLoadingCache<String> stringCache = Caches.newRedisLoadingCache(1, uri, "stringCache", String.class, (key) -> {
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
    SimpleLoadingCache<String> stringCache = Caches.newRedisLoadingCache(1, uri, "stringCache", String.class, (key) -> {
      // we count each load via an Atomic Long
      loadCount.incrementAndGet();
      return key + "-value";
    });

    // we try to load the same key with 100 threads in parallel
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    for (int i = 0; i < 100; i++) {
      executorService.submit(() -> {
        stringCache.getUnchecked("key");
      });
    }

    executorService.shutdown();
    assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));

    // ... only 1 must succeed
    assertEquals(1, loadCount.get());
  }

}