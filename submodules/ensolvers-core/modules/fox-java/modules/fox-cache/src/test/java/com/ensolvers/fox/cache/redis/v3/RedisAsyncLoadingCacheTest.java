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

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisAsyncLoadingCacheTest {

  private static Logger logger = CoreLogger.getLogger(RedisConsumableListCacheTest.class);

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void testErrorInLoader() throws InterruptedException {
    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    SimpleLoadingCache<String> cache = Caches.newGuavaBackedRedisAsyncCache(1, 1, uri, "stringCache", Caches.type(String.class), (key) -> {
      throw new RuntimeException("Exception");
    });

    assertThrows(Exception.class, () -> cache.getUnchecked("String"));
  }

  @Test
  public void testAsyncLoading() throws InterruptedException {
    AtomicLong loadCount = new AtomicLong(0);

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    SimpleLoadingCache<String> stringCache = Caches.newRedisAsyncLoadingCache(1, uri, "stringCache", Caches.type(String.class), (key) -> {
      loadCount.incrementAndGet();

      // we simulate a 1sec wait
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return key + "-value";
    });

    // 10 get operations are tried...
    long timestamp = new Date().getTime();

    // first load try, 1 sec must be passed (since the first load takes that
    stringCache.getUnchecked("key");
    assertTrue(new Date().getTime() - timestamp >= 1000);
    timestamp = new Date().getTime();

    // second load, no delay
    stringCache.getUnchecked("key");
    assertFalse(new Date().getTime() - timestamp < 10);

    // we wait 1 sec so the entry will expire
    Thread.sleep(1000);

    // next load must be immediate despite the entry expired
    stringCache.getUnchecked("key");
    assertFalse(new Date().getTime() - timestamp < 10);

    // in the whole process, 2 loads must have been triggered, the sync one and the one
    // triggered by the async process. We wait 100msec just to ensure that the async
    // process was triggered
    Thread.sleep(100);
    assertEquals(2, loadCount.incrementAndGet());
  }

}