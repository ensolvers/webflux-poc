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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
class RedisConsumableListCacheTest {

  private static final Logger logger = CoreLogger.getLogger(RedisConsumableListCacheTest.class);

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @Test
  public void singleThreadGet() throws Exception {
    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableListCache<GenericObject> list = new RedisConsumableListCache<>(uri, GenericObject.class, (name) -> this.generateObjects(100), 3600);

    int count = 0;

    for (int i = 0; i < 50; i++) {
      List<GenericObject> sublist = list.getFromList("myList", 2);
      assertEquals(2, sublist.size());
      count += sublist.size();
    }

    assertEquals(100, count);
  }

  @Test
  public void multiThreadGet() throws Exception {
    AtomicInteger consumedItems = new AtomicInteger();
    AtomicInteger loadingCalls = new AtomicInteger();

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableListCache<GenericObject> list = new RedisConsumableListCache<>(uri, GenericObject.class, (name) -> {
      loadingCalls.incrementAndGet();
      return this.generateObjects(100);
    }, 3600);

    ExecutorService executor = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 50; i++) {
      executor.execute(() -> {
        List<GenericObject> sublist = null;
        try {
          sublist = list.getFromList("myList", 2);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertEquals(2, sublist.size());
        consumedItems.addAndGet(sublist.size());
      });
    }

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    // we ensure that there are no remaining items in the list
    assertEquals(0, list.getFromList("myList", 2).size());

    // we ensure that all items have been consumed
    assertEquals(100, consumedItems.get());

    // we ensure that only 1 call has been made to the loader
    assertEquals(1, loadingCalls.get());
  }

  @Test
  public void testExpiration() throws Exception {
    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableListCache<GenericObject> consumableList = new RedisConsumableListCache<>(uri, GenericObject.class, (name) -> this.generateObjects(100), 3);

    List<GenericObject> list = consumableList.getFromList("myList", 2);
    assertEquals(0, list.get(0).getIntegerProperty());
    list = consumableList.getFromList("myList", 2);
    assertEquals(2, list.get(0).getIntegerProperty());

    // we wait until cache expiration
    Thread.sleep(5000);

    // then we get the next two elements, that must be the first two we already got in the beginning - since list had to be
    // reloaded
    list = consumableList.getFromList("myList", 2);
    assertEquals(0, list.get(0).getIntegerProperty());
  }

  @Test
  public void testEmptyListLoading() throws Exception {
    AtomicInteger loadingCalls = new AtomicInteger();

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableListCache<GenericObject> list = new RedisConsumableListCache<>(uri, GenericObject.class, (name) -> {
      loadingCalls.incrementAndGet();
      return List.of();
    }, 3600);

    // we try to load an empty list three times, no results
    assertEquals(0, list.getFromList("myList", 2).size());
    assertEquals(0, list.getFromList("myList", 2).size());
    assertEquals(0, list.getFromList("myList", 2).size());

    // and only 1 loader call, since an empty list is still a valid
    assertEquals(1, loadingCalls.get());
  }

  @Test
  public void stressTest() throws Exception {
    AtomicInteger consumedItems = new AtomicInteger();

    String uri = "redis://localhost:" + redisContainer.getMappedPort(6379) + "/0";
    RedisConsumableListCache<GenericObject> list = new RedisConsumableListCache<>(uri, GenericObject.class, (name) -> {
      return this.generateObjects(50000);
    }, 3600);

    ExecutorService executor = Executors.newFixedThreadPool(10);

    for (int i = 0; i < 25000; i++) {
      executor.execute(() -> {
        List<GenericObject> sublist = null;
        try {
          sublist = list.getFromList("myList", 2);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertEquals(2, sublist.size());
        consumedItems.addAndGet(sublist.size());

        if (consumedItems.get() % 1000 == 0) {
          logger.info("Consumed items: {}", consumedItems.get());
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    // we ensure that there are no remaining items in the list
    assertEquals(0, list.remainingItems("myList"));
    assertEquals(0, list.getFromList("myList", 2).size());

    // we ensure that all items have been consumed
    assertEquals(50000, consumedItems.get());
  }

  private List<GenericObject> generateObjects(int count) {
    return IntStream.range(0, count).mapToObj(i -> new GenericObject("s" + i, i)).collect(Collectors.toList());
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