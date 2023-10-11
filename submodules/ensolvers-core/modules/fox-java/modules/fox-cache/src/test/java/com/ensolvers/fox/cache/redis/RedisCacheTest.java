package com.ensolvers.fox.cache.redis;

import static org.junit.jupiter.api.Assertions.*;

import com.ensolvers.fox.cache.TestClass;
import com.ensolvers.fox.cache.exception.CacheExecutionException;
import com.ensolvers.fox.cache.exception.CacheSerializingException;
import com.ensolvers.fox.cache.redis.v2.RedisListAsyncCache;
import com.ensolvers.fox.cache.redis.v2.RedisRegularAsyncCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class RedisCacheTest {

  @Container
  public GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @AfterEach
  public void clear() {
    factory.destroy();
  }

  RedisClient client;
  RedisCacheFactoryTest factory;
  ObjectMapper objectMapper;

  @BeforeEach
  public void setUp() {
    this.client = RedisClient.create("redis://localhost:" + redisContainer.getMappedPort(6379) + "/0");
    this.factory = new RedisCacheFactoryTest(client);
    this.objectMapper = new ObjectMapper();
  }

  @Test
  void redisRegularAsyncCacheTestCase() throws Exception {
    AtomicInteger loaderCalls = new AtomicInteger(0);
    RedisRegularAsyncCache<String, String> cache = this.factory.getRegularAsyncCache("testRegularAsyncCache", 20, 5, Function.identity(), (k) -> {
      try {
        Thread.sleep(8000);
        loaderCalls.incrementAndGet();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return System.currentTimeMillis() + "";
    }, String.class);

    ExecutorService ex = Executors.newFixedThreadPool(4);

    AtomicReference<String> value1 = new AtomicReference<>("");
    AtomicReference<String> value2 = new AtomicReference<>("");

    CountDownLatch latch = new CountDownLatch(4);
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch.countDown();
    });
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch.countDown();
    });
    latch.await();

    assertEquals(value1.get(), cache.get("key1"));
    assertEquals(value2.get(), cache.get("key2"));
    assertEquals(2, loaderCalls.get());
    // Wait until logical expiration
    Thread.sleep(7000);

    // Execute async loading
    CountDownLatch latch2 = new CountDownLatch(4);
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch2.countDown();
    });
    latch2.await();

    Thread.sleep(2000);

    // Old value stills in the cache until async loading ends
    assertEquals(value1.get(), cache.get("key1"));
    assertEquals(value2.get(), cache.get("key2"));

    // Wait until async loading ends
    Thread.sleep(9000);
    assertNotEquals(value1.get(), cache.get("key1"));
    assertNotEquals(value2.get(), cache.get("key2"));
    assertEquals(4, loaderCalls.get());
  }

  @Test
  void redisRegularCacheTestCase() {
    RedisRegularCache<String> cache = this.factory.getRegularCache("testRegularCacheString", 5, String.class);
    RedisRegularCache<String> cache2 = this.factory.getRegularCache("testRegularCacheString2", 5, String.class);

    assertFalse(cache.keyExists("testKey-1"));
    cache.set("testKey-1", "testValue-1");
    assertTrue(cache.keyExists("testKey-1"));

    cache.set("testKey-2", "testValue-2");
    cache.set("testKey-3", "testValue-3");
    cache2.set("testKey-1", "testValue-1");
    cache2.set("testKey-2", "testValue-2");

    // Neither the key nor the value can be null
    assertThrows(IllegalArgumentException.class, () -> cache.set("shouldRaiseException", null));
    assertThrows(IllegalArgumentException.class, () -> cache.set(null, null));
    assertThrows(IllegalArgumentException.class, () -> cache.set(null, "shouldRaiseException"));

    assertEquals("testValue-1", cache.get("testKey-1"));
    assertEquals("testValue-2", cache.get("testKey-2"));
    assertEquals("testValue-1", cache.get("testKey-1"));
    assertEquals("testValue-2", cache.get("testKey-2"));

    cache.invalidateAll();
    assertNull(cache.get("testKey-1"));
    assertNull(cache.get("testKey-2"));
    assertNotNull(cache2.get("testKey-1"));
    assertNotNull(cache2.get("testKey-2"));
  }

  @Test
  void redisListCacheTestCase() {
    RedisListCache<String> cache = this.factory.getListCache("testListCacheString", 1, String.class);
    RedisListCache<String> cache2 = this.factory.getListCache("testListCacheString2", 3, String.class);

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-2", "testValue-1");
    cache.push("testKey-2", "testValue-2");
    cache.push("testKey-2", "testValue-3");

    List<String> cache2List = new ArrayList<>();
    cache2List.add("repeatedValue-1");
    cache2List.add("repeatedValue-1");
    cache2.push("testKey-1", cache2List);
    List<String> emptyList = new ArrayList<>();
    // Neither the key nor the value can be null nor empty
    assertThrows(IllegalArgumentException.class, () -> cache.push("shouldRaiseException", (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, "shouldRaiseException"));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, emptyList));

    assertEquals(2, cache.get("testKey-1").size());
    assertEquals(3, cache.get("testKey-2").size());
    assertEquals(2, cache2.get("testKey-1").size());

    cache.invalidateAll();
    assertEquals(new ArrayList<>(), cache.get("testKey-1"));
    assertEquals(new ArrayList<>(), cache.get("testKey-2"));
    assertEquals(cache2List, cache2.get("testKey-1"));
    cache2.invalidateAll();
    assertEquals(new ArrayList<>(), cache2.get("testKey-1"));
  }

  @Test
  void redisListAsyncCacheTestCase() throws Exception {
    AtomicInteger loaderCalls = new AtomicInteger(0);
    RedisListAsyncCache<String, String> cache = this.factory.getListAsyncCache("testListCacheString", 20, 5, (k) -> {
      try {
        Thread.sleep(8000);
        loaderCalls.incrementAndGet();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      return List.of(System.currentTimeMillis() + "");
    }, String.class);

    ExecutorService ex = Executors.newFixedThreadPool(4);

    AtomicReference<List<String>> value1 = new AtomicReference<>(new ArrayList<>());
    AtomicReference<List<String>> value2 = new AtomicReference<>(new ArrayList<>());

    CountDownLatch latch = new CountDownLatch(4);
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch.countDown();
    });
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch.countDown();
    });
    latch.await();

    assertEquals(value1.get().get(0), cache.get("key1").get(0));
    assertEquals(value2.get().get(0), cache.get("key2").get(0));
    assertEquals(1, cache.size("key1"));
    assertEquals(1, cache.size("key2"));
    assertEquals(2, loaderCalls.get());
    // Wait until logical expiration
    Thread.sleep(7000);

    // Execute async loading
    CountDownLatch latch2 = new CountDownLatch(4);
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value1.set(cache.get("key1"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch2.countDown();
    });
    ex.submit(() -> {
      value2.set(cache.get("key2"));
      latch2.countDown();
    });
    latch2.await();

    Thread.sleep(2000);

    // Old value stills in the cache until async loading ends
    assertEquals(value1.get().get(0), cache.get("key1").get(0));
    assertEquals(value2.get().get(0), cache.get("key2").get(0));
    assertEquals(1, cache.size("key1"));
    assertEquals(1, cache.size("key2"));

    // Wait until async loading ends
    Thread.sleep(9000);
    assertNotEquals(value1.get().get(0), cache.get("key1").get(0));
    assertNotEquals(value2.get().get(0), cache.get("key2").get(0));
    assertEquals(1, cache.size("key1"));
    assertEquals(1, cache.size("key2"));
    assertEquals(4, loaderCalls.get());
  }

  @Test
  void redisSetCacheTestCase() {
    RedisSetCache<String> cache = this.factory.getSetCache("testSetCacheString", 1, String.class);
    RedisSetCache<String> cache2 = this.factory.getSetCache("testSetCacheString2", 2, String.class);
    Set<String> cache2set = new HashSet<>(Collections.emptySet());
    cache2set.add("repeatedValue-1");

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-2", "testValue-1");
    cache.push("testKey-2", "testValue-2");
    cache.push("testKey-2", "testValue-3");

    List<String> cache2List = new ArrayList<>();
    cache2List.add("repeatedValue-1");
    cache2List.add("repeatedValue-1");
    cache2.push("testKey-1", cache2List);
    List<String> emptyList = new ArrayList<>();
    // Neither the key nor the value can be null nor empty
    assertThrows(IllegalArgumentException.class, () -> cache.push("shouldRaiseException", (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, (String) null));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, "shouldRaiseException"));
    assertThrows(IllegalArgumentException.class, () -> cache.push(null, emptyList));

    assertEquals(2, cache.get("testKey-1").size());
    assertEquals(3, cache.get("testKey-2").size());
    // No repeated values on set
    assertEquals(1, cache2.get("testKey-1").size());

    cache.invalidateAll();
    assertEquals(Collections.EMPTY_SET, cache.get("testKey-1"));
    assertEquals(Collections.EMPTY_SET, cache.get("testKey-2"));
    assertEquals(cache2set, cache2.get("testKey-1"));
    cache2.invalidateAll();
    assertEquals(Collections.EMPTY_SET, cache2.get("testKey-1"));
  }

  @Test
  void testMaxEntriesPerBlockLimitingCorrectly() {
    RedisLimitedCache<String> cache = this.factory.getLimitedListCache("testListCacheString", 0, String.class, 3);

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-1", "testValue-3");

    assertEquals(3, cache.get("testKey-1").size());
    cache.push("testKey-1", "testValue-4");
    cache.push("testKey-1", "testValue-5");

    assertEquals(3, cache.get("testKey-1").size());

    cache.invalidateAll();
  }

  @Test
  void testLimitedCacheReplacingOldElementsOnLimitReached() {
    RedisLimitedCache<String> cache = this.factory.getLimitedListCache("testListCacheString", 0, String.class, 3);

    cache.push("testKey-1", "testValue-1");
    cache.push("testKey-1", "testValue-2");
    cache.push("testKey-1", "testValue-3");
    cache.push("testKey-1", "testValue-4");
    cache.push("testKey-1", "testValue-5");

    assertEquals("testValue-5", cache.get("testKey-1").get(0));
    assertEquals("testValue-4", cache.get("testKey-1").get(1));
    assertEquals("testValue-3", cache.get("testKey-1").get(2));

    cache.push("testKey-1", "testValue-6");
    assertEquals("testValue-6", cache.get("testKey-1").get(0));

    assertThrows(IndexOutOfBoundsException.class, () -> cache.get("testKey-1").get(3));
    assertThrows(IndexOutOfBoundsException.class, () -> cache.get("testKey-1").get(4));

    cache.invalidateAll();
  }

  // Dummy custom deserializer test that adds dummy data to the DTO
  @Test
  void testCustomSerializer() {
    RedisListCache<TestClass> cache = this.factory.getListCache("testListCache", 100, TestClass.class, objectMapper::writeValueAsString, (string) -> {
      TestClass testClass = objectMapper.readValue(string, TestClass.class);
      testClass.setIntegerValue(testClass.getIntegerValue() + 1000);
      testClass.setStringValue("From custom deserializer" + testClass.getStringValue());

      return testClass;
    });

    cache.push("123", new TestClass(" - 1", 1));
    cache.push("123", new TestClass(" - 2", 2));

    assertEquals("From custom deserializer - 1", cache.get("123").get(1).getStringValue());
    assertEquals("From custom deserializer - 2", cache.get("123").get(0).getStringValue());
    assertEquals(Integer.valueOf(1001), cache.get("123").get(1).getIntegerValue());
    assertEquals(Integer.valueOf(1002), cache.get("123").get(0).getIntegerValue());

    cache.push("abc", new TestClass(" - 3", 3));
    assertEquals("From custom deserializer - 3", cache.get("abc").get(0).getStringValue());
    assertEquals(Integer.valueOf(1003), cache.get("abc").get(0).getIntegerValue());

    cache.invalidateAll();
  }

  @Test
  void testRedisCachePropagatesSerializingException() {
    RedisListCache<TestClass> cache = this.factory.getListCache("testListCache", 100, TestClass.class, objectMapper::writeValueAsString,
        (string) -> objectMapper.readValue(string, TestClass.class));

    cache.push("123", new TestClass());
    cache.get("123");
    cache.push("abc", new TestClass());
    cache.get("abc");

    this.factory.removeCacheFromList("testListCache");

    cache = this.factory.getListCache("testListCache", 100, TestClass.class, (o) -> {
      throw new CacheExecutionException("Redis Test Case");
    }, (s) -> {
      throw new CacheExecutionException("Redis Test Case");
    });

    RedisListCache<TestClass> finalCache = cache;

    assertThrows(CacheSerializingException.class, () -> finalCache.get("123"));

    assertThrows(RuntimeException.class, () -> finalCache.push("123", new TestClass()));
    assertThrows(RuntimeException.class, () -> finalCache.push("abc", new TestClass()));

    cache.invalidateAll();
  }

  @Test
  void redisTypesTestCase() {
    RedisRegularCache<Long> longCache;
    RedisRegularCache<Integer> integerCache;
    RedisRegularCache<Character> characterCache;
    RedisRegularCache<Boolean> booleanCache;

    longCache = this.factory.getRegularCache("testRegularCacheLong", 1, Long.class);
    integerCache = this.factory.getRegularCache("testRegularCacheInteger", 1, Integer.class);
    characterCache = this.factory.getRegularCache("testRegularCacheCharacter", 1, Character.class);
    booleanCache = this.factory.getRegularCache("testRegularCacheBoolean", 1, Boolean.class);

    Long longKey = 1L;
    Long longValue = 7L;
    longCache.set(longKey.toString(), longValue);
    assertEquals(longValue, longCache.get(longKey.toString()));

    Integer integerKey = 2;
    Integer integerValue = 5;
    integerCache.set(integerKey.toString(), integerValue);
    assertEquals(integerValue, integerCache.get(integerKey.toString()));

    Character charKey = 'f';
    Character charValue = 'm';
    characterCache.set(charKey.toString(), charValue);
    assertEquals(charValue, characterCache.get(charKey.toString()));

    Boolean boolKey = true;
    Boolean boolValue = false;
    booleanCache.set(boolKey.toString(), boolValue);
    assertFalse(booleanCache.get(boolKey.toString()));
  }

  @Test
  void redisTimeoutTestCase() throws InterruptedException {
    RedisRegularCache<String> regularCache;
    RedisListCache<String> listCache;
    RedisSetCache<String> setCache;

    regularCache = this.factory.getRegularCache("testRegularCacheString3", 3, String.class);
    listCache = this.factory.getListCache("testListCacheString2", 3, String.class);
    setCache = this.factory.getSetCache("testSetCacheString3", 3, String.class);

    String regularCacheKey = "regularCacheKey";
    String regularCacheValue = "regularCacheValue";

    String listCacheKey = "listCacheKey";
    String listCacheValue = "listCacheValue";

    String setCacheKey = "setCacheKey";
    String setCacheValue = "setCacheValue";

    regularCache.set(regularCacheKey, regularCacheValue);
    listCache.push(listCacheKey, listCacheValue);
    setCache.push(setCacheKey, setCacheValue);

    assertNotNull(regularCache.get(regularCacheKey));
    assertEquals(1, listCache.get(listCacheKey).size());
    assertEquals(1, setCache.get(setCacheKey).size());

    TimeUnit.SECONDS.sleep(4);

    assertNull(regularCache.get(regularCacheKey));
    assertEquals(0, listCache.get(listCacheKey).size());
    assertEquals(0, setCache.get(setCacheKey).size());

    // Reset TTL test
    regularCache.set(regularCacheKey, regularCacheValue);
    listCache.push(listCacheKey, listCacheValue);
    setCache.push(setCacheKey, setCacheValue);

    TimeUnit.SECONDS.sleep(2);

    regularCache.resetTTL(regularCacheKey);
    listCache.resetTTL(listCacheKey);
    setCache.resetTTL(setCacheKey);

    TimeUnit.SECONDS.sleep(2);

    assertNotNull(regularCache.get(regularCacheKey));
    assertEquals(1, listCache.get(listCacheKey).size());
    assertEquals(1, setCache.get(setCacheKey).size());
  }

  @Test
  void redisCustomClassCacheTestCase() {
    RedisRegularCache<TestClass> cache;
    cache = this.factory.getRegularCache("testRegularCacheTestClass", 3, TestClass.class);

    String cacheKey = "regularCacheKey";
    TestClass cacheValue = new TestClass(1L, "someString", 2, 3L);

    cache.set(cacheKey, cacheValue);
    assertEquals(cacheValue, cache.get(cacheKey));
  }

  @Test
  void redisCustomSerializerTestCase() {
    RedisRegularCache<TestClass> cache;
    cache = this.factory.getRegularCache("testRegularCacheTestClass", 3, TestClass.class,
        // serialize the object using static field order and "|" as a separator
        (TestClass instance) -> instance.getId() + "|" + instance.getStringValue() + "|" + instance.getIntegerValue() + "|" + instance.getLongValue(),
        serialized -> {
          String[] parts = serialized.split("\\|");
          return new TestClass(Long.parseLong(parts[0]), parts[1], Integer.parseInt(parts[2]), Long.parseLong(parts[3]));
        });

    String cacheKey = "regularCacheKey";
    TestClass cacheValue = new TestClass(1L, "someString", 2, 3L);

    cache.set(cacheKey, cacheValue);
    assertEquals(cacheValue, cache.get(cacheKey));
  }
}
