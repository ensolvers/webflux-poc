package com.ensolvers.fox.services.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JsonMapTest {

  private String keyValue = "{\"oneKey\":\"OneValue\"}";
  private String keys = "{\"1\":\"A\",\"2\":\"B\",\"3\":\"C\"}";

  @Test
  void emptyJsonTest() {
    assertEquals("{}", new JsonMap().asJson());
  }

  @Test
  void simpleJsonTest() {
    assertEquals(keyValue, new JsonMap("oneKey", "OneValue").asJson());
  }

  @Test
  void simpleMap2JsonTest() {
    assertEquals(keys, new JsonMap(Map.of("1", "A", "2", "B", "3", "C")).asJson());
  }

  @Test
  void simpleBuildJsonTest() {
    assertEquals(keys, JsonMap.fromJson(keys).asJson());
  }

  @Test
  void simpleBuildFromStreamJsonTest() {
    assertEquals(keys, JsonMap.fromJson(new ByteArrayInputStream(keys.getBytes())).asJson());
  }

  @Test
  void simpleBuildFromSortedArrayJsonTest() throws Exception {
    JsonMap[] jsons = JsonMap.fromJsonArray("[ {\"id\": \"1\"}, {\"id\": \"3\"}, {\"id\": \"2\"}, {\"id\": \"0\"} ]", "id");

    assertEquals("{\"id\":\"0\"}", jsons[0].asJson());
    assertEquals("{\"id\":\"1\"}", jsons[1].asJson());
    assertEquals("{\"id\":\"2\"}", jsons[2].asJson());
    assertEquals("{\"id\":\"3\"}", jsons[3].asJson());
  }

  @Test
  void addPropJsonTest() {
    assertEquals("{\"oneKey\":\"OneValue\",\"secondKey\":\"SecondValue\"}", new JsonMap("oneKey", "OneValue").addProp("secondKey", "SecondValue").asJson());
  }

  @Test
  void addMapPropJsonTest() {
    assertEquals("{\"1\":\"A\",\"2\":\"B\",\"3\":\"C\",\"oneKey\":\"OneValue\"}",
        new JsonMap("oneKey", "OneValue").addProps(Map.of("1", "A", "2", "B", "3", "C")).asJson());
  }

  @Test
  void removePropJsonTest() {
    assertEquals("{\"1\":\"A\",\"3\":\"C\"}", new JsonMap().addProps(Map.of("1", "A", "2", "B", "3", "C")).removeProp("2").asJson());
  }

  @Test
  void appendPropJsonTest() {
    assertEquals("{\"indexKey\":[\"firstValue\"],\"oneKey\":\"OneValue\"}", new JsonMap("oneKey", "OneValue").appendProp("indexKey", "firstValue").asJson());
    assertEquals("{\"indexKey\":[\"firstValue\",\"secondValue\"],\"oneKey\":\"OneValue\"}",
        new JsonMap("oneKey", "OneValue").appendProp("indexKey", "firstValue").appendProp("indexKey", "secondValue").asJson());
  }

  @Test
  void simplePathJsonTest() {
    assertEquals("B", JsonMap.fromJson(keys).path("2"));

  }

  @Test
  void complexPathJsonTest() {
    assertEquals("valBB", JsonMap.fromJson("{\"a\": \"valA\"," + " \"b\": { \"ba\": \"valBA\",\n" + "          \"bb\": \"valBB\"} }").path("b bb"));
  }

  @Test
  void complexIndexPathJsonTest() {
    JsonMap jsonMap = JsonMap
        .fromJson("{\"a\": \"valA\"," + " \"i\": [ { \"1a\": \"val1A\", \"1b\": \"val1B\"}," + "          { \"2a\": \"val2A\", \"2b\": \"val2B\"} ] }");
    assertEquals("val1B", jsonMap.path("i 0 1b"));
    assertEquals("val2A", jsonMap.path("i 1 2a"));
  }

  @Test
  void setInexistentPathJsonTest() {
    assertEquals("{\"1\":\"A\",\"2\":\"B\",\"3\":\"C\",\"4\":\"X\"}", JsonMap.fromJson(keys).setPath("X", "4").asJson());
  }

  @Test
  void alterPathJsonTest() {
    assertEquals("{\"1\":\"X\",\"2\":\"B\",\"3\":\"C\"}", JsonMap.fromJson(keys).setPath("X", "1").asJson());
  }

  @Test
  void alterComplexIndexPathJsonTest() {
    JsonMap jsonMap = JsonMap
        .fromJson("{\"a\": \"valA\"," + " \"i\": [ { \"1a\": \"val1A\", \"1b\": \"val1B\"}," + "          { \"2a\": \"val2A\", \"2b\": \"val2B\"} ] }");
    assertEquals("newVal", jsonMap.setPath("newVal", "i 0 1b").path("i 0 1b"));
    assertEquals("new2Al", jsonMap.setPath("new2Al", "i 1 2a").path("i 1 2a"));
  }

}
