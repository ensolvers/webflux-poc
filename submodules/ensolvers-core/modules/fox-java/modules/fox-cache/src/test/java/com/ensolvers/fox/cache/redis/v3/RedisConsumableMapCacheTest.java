package com.ensolvers.fox.cache.redis.v3;

import com.ensolvers.fox.services.logging.CoreLogger;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class RedisConsumableMapCacheTest {

  private static final Logger logger = CoreLogger.getLogger(RedisConsumableMapCacheTest.class);

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void singleThreadGet() throws Exception {
    AtomicInteger consumedItems = new AtomicInteger();
    AtomicInteger loadingCalls = new AtomicInteger();

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableMapCache<GenericObject> map = new RedisConsumableMapCache<>(uri, GenericObject.class, (name) -> {
      loadingCalls.incrementAndGet();
      return this.generateMap(100);
    }, 3600);

    List<String> keys = this.generateKeys(100);

    for (int i = 0; i < 100; i += 2) {
      Map<String, GenericObject> objects = map.consumeEntries("myMap", Set.of("k" + i, "k" + (i + 1)));
      assertEquals(2, objects.size());
      consumedItems.addAndGet(objects.size());
    }

    // we ensure that all items have been consumed
    assertEquals(100, consumedItems.get());

    // we ensure that only 1 call has been made to the loader
    assertEquals(1, loadingCalls.get());
  }

  @Test
  public void multiThreadGet() throws Exception {
    ConcurrentMap<String, Integer> keyCount = new ConcurrentHashMap<>();
    AtomicInteger loadingCalls = new AtomicInteger();

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableMapCache<GenericObject> map = new RedisConsumableMapCache<>(uri, GenericObject.class, (name) -> {
      loadingCalls.incrementAndGet();
      return this.generateMap(100);
    }, 3600);

    ExecutorService executor = Executors.newFixedThreadPool(10);
    List<String> keys = this.generateKeys(100);

    for (int i = 0; i < 100; i += 2) {
      int finalI = i;
      executor.execute(() -> {
        Map<String, GenericObject> objects = null;
        try {
          objects = map.consumeEntries("myMap", Set.of("k" + finalI, "k" + (finalI + 1), "k" + (finalI + 2)));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        // if the fetch was successful, we ensure that only 3 items have been fetched and...
        assertEquals(3, objects.size());
        // ... we count each of them in the shared dictionary
        objects.keySet().forEach(k -> {
          keyCount.computeIfPresent(k, (key, value) -> value + 1);
          keyCount.putIfAbsent(k, 1);

        });
      });
    }

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    // at the end, each key must have been computed only once
    // assertTrue(keyCount.values().stream().allMatch(v -> v == 1));

    // we ensure that only 1 call has been made to the loader
    assertEquals(1, loadingCalls.get());
  }

  private Map<String, GenericObject> generateMap(int count) {
    return IntStream.range(0, count).boxed().collect(Collectors.toMap(i -> "k" + i, i -> new GenericObject("s" + i, i)));
  }

  private List<String> generateKeys(int count) {
    return IntStream.range(0, count).boxed().map(i -> "k" + i).collect(Collectors.toList());
  }

  @Getter
  @Setter
  private static class GenericObject {
    String stringProperty;
    Integer integerProperty;

    public GenericObject() {
    }

    public GenericObject(String stringProperty, Integer integerProperty) {
      this.stringProperty = stringProperty;
      this.integerProperty = integerProperty;
    }
  }
}