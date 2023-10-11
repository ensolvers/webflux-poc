package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.services.logging.CoreLogger;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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
class RedisAtomicLongCacheTest {

  private static final CoreLogger logger = CoreLogger.getLogger(RedisConsumableListCacheTest.class);

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void testPureAtomicLong() throws InterruptedException {
    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";

    Config config = new Config();
    config.useSingleServer().setAddress(uri);
    RedissonClient redisson = Redisson.create(config);

    // Initialize the AtomicLong
    RAtomicLong atomicLong = redisson.getAtomicLong("testAtomicLong");
    atomicLong.set(0);

    AtomicLong executions = new AtomicLong(0);

    // we try to load the same key with 100 threads in parallel
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    for (int i = 0; i < 1000; i++) {
      executorService.submit(() -> {
        logger.info("Current: " + executions.incrementAndGet() + ", Value: " + atomicLong.addAndGet(1));
      });
    }

    // Shutdown the thread pool
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    assertEquals(1000, executions.get());
    assertEquals(1000, atomicLong.get());

    redisson.shutdown();
  }

  @Test
  public void testRedisAtomicLongCache() throws InterruptedException {
    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";

    Config config = new Config();
    config.useSingleServer().setAddress(uri);
    RedissonClient redisson = Redisson.create(config);

    AtomicLong executions = new AtomicLong(0);
    AtomicLong loads = new AtomicLong(0);

    // Initialize the AtomicLong
    RedisAtomicLongCache ral = new RedisAtomicLongCache(uri, "RAL", key -> {
      loads.incrementAndGet();
      return 1L;
    }, 1);

    // we try to load the same key with 100 threads in parallel
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    for (int i = 0; i < 10000; i++) {
      executorService.submit(() -> {
        logger.info("Current: " + executions.incrementAndGet() + ", Value: " + ral.addAndGet("testAtomicLong", 1));
      });
    }

    // Shutdown the thread pool
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);

    assertEquals(10000, executions.get());
    assertEquals(10001, ral.getUnchecked("testAtomicLong"));
    assertEquals(1, loads.get());

    redisson.shutdown();

  }

}