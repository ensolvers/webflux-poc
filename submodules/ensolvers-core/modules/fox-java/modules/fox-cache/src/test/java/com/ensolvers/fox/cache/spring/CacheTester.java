package com.ensolvers.fox.cache.spring;

import com.ensolvers.fox.cache.exception.CacheInvalidArgumentException;
import com.ensolvers.fox.cache.utils.Profile;
import com.ensolvers.fox.cache.spring.context.objects.SampleComponent;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CacheTester {
  public static void testGet(SampleComponent sampleComponent) {
    sampleComponent.invalidateAll();
    sampleComponent.resetStats();

    String time1a = sampleComponent.timeWithCache();
    checkMissedHit(1, sampleComponent);

    String time1b = sampleComponent.timeWithCache();
    checkHit(sampleComponent);

    assertEquals(time1a, sampleComponent.timeWithCache());
    checkHit(sampleComponent);
    assertEquals(time1b, sampleComponent.timeWithCache());
    checkHit(sampleComponent);
    assertEquals(time1a, time1b);

    String time2a = sampleComponent.timeWithCacheAndSimpleParams("time2a");
    checkMissedHit(1, sampleComponent);

    String time2b = sampleComponent.timeWithCacheAndSimpleParams("time2b");
    checkMissedHit(1, sampleComponent);

    assertEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
    checkHit(sampleComponent);
    assertEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
    checkHit(sampleComponent);
    assertNotEquals(time2a, time2b);

    String time3a = sampleComponent.timeWithCacheAndSimpleParams("time3a", 1);
    checkMissedHit(1, sampleComponent);

    String time3b = sampleComponent.timeWithCacheAndSimpleParams("time3b", 2);
    checkMissedHit(1, sampleComponent);

    assertEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
    checkHit(sampleComponent);
    assertEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
    checkHit(sampleComponent);
    assertNotEquals(time3a, time3b);

    String time4a = sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true);
    checkMissedHit(1, sampleComponent);

    String time4b = sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false);
    checkMissedHit(1, sampleComponent);

    assertEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
    checkHit(sampleComponent);
    assertEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
    checkHit(sampleComponent);
    assertNotEquals(time4a, time4b);

    Date date1 = new Date();
    Date date2 = new Date();

    String time5a = sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1);
    checkMissedHit(1, sampleComponent);

    String time5b = sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2);
    checkMissedHit(1, sampleComponent);

    assertEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
    checkHit(sampleComponent);
    assertEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));
    checkHit(sampleComponent);
    assertNotEquals(time5a, time5b);
  }

  public static void testGetComplexObjects(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    Profile profile1 = sampleComponent.profileWithCacheAndSimpleParams("profile1");
    checkMissedHit(1, sampleComponent);

    Profile profile2 = sampleComponent.profileWithCacheAndSimpleParams("profile2");
    checkMissedHit(1, sampleComponent);

    assertEquals(profile1, sampleComponent.profileWithCacheAndSimpleParams("profile1"));
    checkHit(sampleComponent);
    assertEquals(profile2, sampleComponent.profileWithCacheAndSimpleParams("profile2"));
    checkHit(sampleComponent);
  }

  public static void testBulkGetComplexObjects(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    // Test list
    List<String> keys1 = new ArrayList<>();
    keys1.add("profiles1a");
    keys1.add("profiles1b");

    List<String> keys2 = new ArrayList<>();
    keys2.add("profiles2a");
    keys2.add("profiles2b");

    Map<String, Profile> profiles1 = sampleComponent.profilesWithCacheAndSimpleParams(keys1);
    checkMissedHit(2, sampleComponent);

    Map<String, Profile> profiles2 = sampleComponent.profilesWithCacheAndSimpleParams(keys2);
    checkMissedHit(2, sampleComponent);

    assertEquals(profiles1, sampleComponent.profilesWithCacheAndSimpleParams(keys1));
    checkHit(sampleComponent);
    assertEquals(profiles2, sampleComponent.profilesWithCacheAndSimpleParams(keys2));
    checkHit(sampleComponent);

    // Test list with repeats
    List<String> keys3 = new ArrayList<>();
    keys3.add("profiles3a");
    keys3.add("profiles3a");
    keys3.add("profiles3b");

    Map<String, Profile> profiles3 = sampleComponent.profilesWithCacheAndSimpleParams(keys3);
    checkMissedHit(2, sampleComponent);

    assertEquals(profiles3, sampleComponent.profilesWithCacheAndSimpleParams(keys3));
    checkHit(sampleComponent);

    // Test set
    Set<String> keys4 = new HashSet<>();
    keys4.add("profiles4a");
    keys4.add("profiles4b");

    Map<String, Profile> profiles4 = sampleComponent.profilesWithCacheAndSimpleParams(keys4);
    checkMissedHit(2, sampleComponent);

    assertEquals(profiles4, sampleComponent.profilesWithCacheAndSimpleParams(keys4));
    checkHit(sampleComponent);

    // Test partial hit
    keys4.add("profiles4c");

    Map<String, Profile> profiles5 = sampleComponent.profilesWithCacheAndSimpleParams(keys4);
    checkMissedHit(1, sampleComponent);

    assertEquals(profiles5, sampleComponent.profilesWithCacheAndSimpleParams(keys4));
    checkHit(sampleComponent);

    // Test empty key list
    sampleComponent.profilesWithCacheAndSimpleParams(new ArrayList<>());
    checkHit(sampleComponent);
  }

  public static void testNullValues(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    Profile profile1 = sampleComponent.getNullFromCacheNullable("profile1");
    checkMissedHit(1, sampleComponent);

    Profile profile2 = sampleComponent.getNullFromCacheNullable("profile2");
    checkMissedHit(1, sampleComponent);

    assertEquals(profile1, sampleComponent.getNullFromCacheNullable("profile1"));
    checkHit(sampleComponent);
    assertEquals(profile2, sampleComponent.getNullFromCacheNullable("profile2"));
    checkHit(sampleComponent);

    assertNull(profile1);
    assertNull(profile2);

    assertThrows(CacheInvalidArgumentException.class, () -> sampleComponent.getNullFromCacheNotNullable("profile3"));
  }

  public static void testNullValuesInBulkGet(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    List<String> keys1 = new ArrayList<>();
    keys1.add("profiles1a");
    keys1.add("profiles1b");

    List<String> keys2 = new ArrayList<>();
    keys2.add("profiles2a");
    keys2.add("profiles2b");

    Map<String, Profile> profiles1 = sampleComponent.profilesWithNullAndCacheNullable(keys1);
    checkMissedHit(2, sampleComponent);

    Map<String, Profile> profiles2 = sampleComponent.profilesWithNullAndCacheNullable(keys2);
    checkMissedHit(2, sampleComponent);

    assertEquals(profiles1, sampleComponent.profilesWithNullAndCacheNullable(keys1));
    checkHit(sampleComponent);
    assertEquals(profiles2, sampleComponent.profilesWithNullAndCacheNullable(keys2));
    checkHit(sampleComponent);

    // Test list with repeats
    List<String> keys3 = new ArrayList<>();
    keys3.add("profiles3a");
    keys3.add("profiles3a");
    keys3.add("profiles3b");

    Map<String, Profile> profiles3 = sampleComponent.profilesWithNullAndCacheNullable(keys3);
    checkMissedHit(2, sampleComponent);

    assertEquals(profiles3, sampleComponent.profilesWithNullAndCacheNullable(keys3));
    checkHit(sampleComponent);

    // Test partial hit
    keys3.add("profiles4c");

    Map<String, Profile> profiles4 = sampleComponent.profilesWithNullAndCacheNullable(keys3);
    checkMissedHit(1, sampleComponent);

    assertEquals(profiles4, sampleComponent.profilesWithNullAndCacheNullable(keys3));
    checkHit(sampleComponent);

    assertThrows(CacheInvalidArgumentException.class, () -> sampleComponent.profilesWithNullAndCacheNotNullable(keys3));
  }

  public static void testPut(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    String stringNumber = sampleComponent.stringNumber("stringNumber1a", "stringNumber1b");
    sampleComponent.decreaseStringNumber("stringNumber1a", "stringNumber1b", stringNumber);
    String decreasedStringNumber = sampleComponent.stringNumber("stringNumber1a", "stringNumber1b");
    assertEquals(String.valueOf(Integer.parseInt(stringNumber) - 1), decreasedStringNumber);
  }

  public static void testInvalidate(SampleComponent sampleComponent) {
    sampleComponent.resetStats();
    sampleComponent.invalidateAll();

    String time1a = sampleComponent.timeWithCache();
    checkMissedHit(1, sampleComponent);

    assertEquals(time1a, sampleComponent.timeWithCache());
    checkHit(sampleComponent);

    String time2a = sampleComponent.timeWithCacheAndSimpleParams("time2a");
    checkMissedHit(1, sampleComponent);

    String time2b = sampleComponent.timeWithCacheAndSimpleParams("time2b");
    checkMissedHit(1, sampleComponent);

    assertEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
    checkHit(sampleComponent);
    assertEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
    checkHit(sampleComponent);
    assertNotEquals(time2a, time2b);

    String time3a = sampleComponent.timeWithCacheAndSimpleParams("time3a", 1);
    checkMissedHit(1, sampleComponent);

    String time3b = sampleComponent.timeWithCacheAndSimpleParams("time3b", 2);
    checkMissedHit(1, sampleComponent);

    assertEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
    checkHit(sampleComponent);
    assertEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
    checkHit(sampleComponent);
    assertNotEquals(time3a, time3b);

    String time4a = sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true);
    checkMissedHit(1, sampleComponent);

    String time4b = sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false);
    checkMissedHit(1, sampleComponent);

    assertEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
    checkHit(sampleComponent);
    assertEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
    checkHit(sampleComponent);
    assertNotEquals(time4a, time4b);

    Date date1 = new Date();
    Date date2 = new Date();
    String time5a = sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1);
    checkMissedHit(1, sampleComponent);
    String time5b = sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2);
    checkMissedHit(1, sampleComponent);

    assertEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
    checkHit(sampleComponent);
    assertEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));
    checkHit(sampleComponent);
    assertNotEquals(time5a, time5b);

    // Invalidate all
    sampleComponent.invalidateAll();
    assertNotEquals(time1a, sampleComponent.timeWithCache());
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time2a, sampleComponent.timeWithCacheAndSimpleParams("time2a"));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time2b, sampleComponent.timeWithCacheAndSimpleParams("time2b"));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time3a, sampleComponent.timeWithCacheAndSimpleParams("time3a", 1));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time3b, sampleComponent.timeWithCacheAndSimpleParams("time3b", 2));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time4a, sampleComponent.timeWithCacheAndSimpleParams("time4a", 1, true));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time4b, sampleComponent.timeWithCacheAndSimpleParams("time4b", 2, false));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time5a, sampleComponent.timeWithCacheAndSimpleParams("time5a", 1, true, date1));
    checkMissedHit(1, sampleComponent);
    assertNotEquals(time5b, sampleComponent.timeWithCacheAndSimpleParams("time5b", 2, false, date2));
    checkMissedHit(1, sampleComponent);

    // Invalidate by param
    String time6a = sampleComponent.timeWithCacheAndSimpleParams("time6a");
    checkMissedHit(1, sampleComponent);

    String time6b = sampleComponent.timeWithCacheAndSimpleParams("time6b");
    checkMissedHit(1, sampleComponent);

    assertEquals(time6a, sampleComponent.timeWithCacheAndSimpleParams("time6a"));
    checkHit(sampleComponent);
    assertEquals(time6b, sampleComponent.timeWithCacheAndSimpleParams("time6b"));
    checkHit(sampleComponent);

    sampleComponent.invalidateWithParam("time6a");

    assertNotEquals(time6a, sampleComponent.timeWithCacheAndSimpleParams("time6a"));
    checkMissedHit(1, sampleComponent);
    assertEquals(time6b, sampleComponent.timeWithCacheAndSimpleParams("time6b"));
    checkHit(sampleComponent);
  }

  private static void checkHit(SampleComponent sampleComponent) {
    assertFalse(sampleComponent.isMissedHit());
    assertEquals(0, sampleComponent.getMissedHits());
  }

  private static void checkMissedHit(int missedHits, SampleComponent sampleComponent) {
    assertTrue(sampleComponent.isMissedHit());
    assertEquals(missedHits, sampleComponent.getMissedHits());
    sampleComponent.resetStats();
  }
}
