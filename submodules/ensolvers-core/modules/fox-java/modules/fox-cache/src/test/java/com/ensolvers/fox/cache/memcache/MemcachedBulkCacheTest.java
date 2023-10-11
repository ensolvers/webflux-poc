package com.ensolvers.fox.cache.memcache;

import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.memcached.MemcachedBulkCache;
import com.ensolvers.fox.cache.utils.Profile;
import net.spy.memcached.MemcachedClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class MemcachedBulkCacheTest {
  @Container
  public GenericContainer<?> memcachedContainer = new GenericContainer<>(DockerImageName.parse("memcached:1.6.10")).withExposedPorts(11211);

  private MemcachedClient memcachedClient;
  private MemcachedBulkCache<Profile> profileCache;
  private MemcachedBulkCache<Profile> profileNullableCache;
  private MemcachedBulkCache<Profile> profileNotNullableCacheWithNull;

  @BeforeEach
  public void initializeCache() throws IOException {
    memcachedClient = new MemcachedClient(new InetSocketAddress(memcachedContainer.getMappedPort(11211)));
    profileCache = new MemcachedBulkCache<>(memcachedClient, id -> Profile.random(),
        ids -> ids.stream().collect(Collectors.toMap(Function.identity(), id -> Profile.random())), "profile", Profile.class, 3600, false);

    profileNullableCache = new MemcachedBulkCache<>(memcachedClient, id -> null, ids -> {
      Map<String, Profile> result = ids.stream().collect(Collectors.toMap(Function.identity(), id -> Profile.random()));
      result.remove((new ArrayList<>(ids)).get(Math.min(1, ids.size() - 1)));
      return result;
    }, "profileNullable", Profile.class, 3600, true);

    profileNotNullableCacheWithNull = new MemcachedBulkCache<>(memcachedClient, id -> null, ids -> {
      Map<String, Profile> result = ids.stream().collect(Collectors.toMap(Function.identity(), id -> Profile.random()));
      result.remove((new ArrayList<>(ids)).get(Math.min(1, ids.size() - 1)));
      return result;
    }, "profileNotNullableWithNull", Profile.class, 3600, false);
  }

  @Test
  void testGetComplexObjects() {
    resetCache();

    Profile profile1 = profileCache.get("profile1");

    Profile profile2 = profileCache.get("profile2");

    assertEquals(profile1, profileCache.get("profile1"));
    assertEquals(profile2, profileCache.get("profile2"));
  }

  @Test
  void testBulkGetComplexObjects() {
    resetCache();

    // Test list
    List<String> keys1 = new ArrayList<>();
    keys1.add("profiles1a");
    keys1.add("profiles1b");

    List<String> keys2 = new ArrayList<>();
    keys2.add("profiles2a");
    keys2.add("profiles2b");

    Map<String, Profile> profiles1 = profileCache.getMap(keys1);
    Map<String, Profile> profiles2 = profileCache.getMap(keys2);

    assertEquals(profiles1, profileCache.getMap(keys1));
    assertEquals(profiles2, profileCache.getMap(keys2));

    // Test list with repeats
    List<String> keys3 = new ArrayList<>();
    keys3.add("profiles3a");
    keys3.add("profiles3a");
    keys3.add("profiles3b");

    Map<String, Profile> profiles3 = profileCache.getMap(keys3);
    assertEquals(profiles3, profileCache.getMap(keys3));

    // Test set
    Set<String> keys4 = new HashSet<>();
    keys4.add("profiles4a");
    keys4.add("profiles4b");

    Map<String, Profile> profiles4 = profileCache.getMap(keys4);
    assertEquals(profiles4, profileCache.getMap(keys4));

    // Test partial hit
    keys4.add("profiles4c");

    Map<String, Profile> profiles5 = profileCache.getMap(keys4);
    assertEquals(profiles5, profileCache.getMap(keys4));

    // Test empty key list
    var profiles6 = profileCache.getMap(new ArrayList<>());
    assertTrue(profiles6.isEmpty());
  }

  @Test
  void testNullValues() {
    resetCache();

    List<String> keys1 = new ArrayList<>();
    keys1.add("profiles1a");
    keys1.add("profiles1b");

    List<String> keys2 = new ArrayList<>();
    keys2.add("profiles2a");
    keys2.add("profiles2b");

    Map<String, Profile> profiles1 = profileNullableCache.getMap(keys1);
    Map<String, Profile> profiles2 = profileNullableCache.getMap(keys2);
    assertEquals(profiles1, profileNullableCache.getMap(keys1));
    assertEquals(profiles2, profileNullableCache.getMap(keys2));

    // Test list with repeats
    List<String> keys3 = new ArrayList<>();
    keys3.add("profiles3a");
    keys3.add("profiles3a");
    keys3.add("profiles3b");

    Map<String, Profile> profiles3 = profileNullableCache.getMap(keys3);
    assertEquals(profiles3, profileNullableCache.getMap(keys3));

    // Test partial hit
    keys3.add("profiles4c");
    Map<String, Profile> profiles4 = profileNullableCache.getMap(keys3);
    assertEquals(profiles4, profileNullableCache.getMap(keys3));

    assertThrows(CacheInvalidArgumentException.class, () -> profileNotNullableCacheWithNull.getMap(keys3));
  }

  @Test
  void testPut() {
    resetCache();

    Profile profile = profileCache.get("profile1");
    Profile profileModified = Profile.random();
    profileCache.put("profile1", profileModified);

    assertNotEquals(profileModified, profile);
    assertEquals(profileModified, profileCache.get("profile1"));
  }

  @Test
  void testInvalidate() {
    Profile profile1 = profileCache.get("profile1");
    Profile profile2 = profileCache.get("profile2");

    assertEquals(profile1, profileCache.get("profile1"));
    assertEquals(profile2, profileCache.get("profile2"));

    profileCache.invalidate("profile1");

    assertNotEquals(profile1, profileCache.get("profile1"));
    assertEquals(profile2, profileCache.get("profile2"));
  }

  private void resetCache() {
    memcachedClient.flush();
  }
}
