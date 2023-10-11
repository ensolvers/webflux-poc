package com.ensolvers.fox.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.apache.commons.io.IOUtils;

/**
 * JsonMap serves to create, build, navigate, alter, read and write Json Strings.
 * <p>
 * <p>
 * <p>
 * Section Constructors:
 * <p>
 * to provide means to create a <code>JsonMap</code> instance
 * <p>
 * an empty one, or with a key, or from a dictionary
 * <p>
 * Please, see the test package
 * <p>
 * <p>
 * <p>
 * Section Builders:
 * <p>
 * to provide means to build(parse) an instance (or an array) of <code>JsonMap</code>
 * <p>
 * from a string or a stream source
 * <p>
 * Please, see the test package
 * <p>
 * <p>
 * <p>
 * Section Instance Protocol:
 * <p>
 * to provide means to interact with an instance to:
 * <p>
 * * add a new key value pair
 * <p>
 * * add a new set of key value pairs by means of a dictionary
 * <p>
 * * add a new position and value in an arrayed key
 * <p>
 * * remove a key and its value
 * <p>
 * * write a String Json representation of the instance
 * <p>
 * Please, see the test package
 * <p>
 * <p>
 * <p>
 * Section Traversing Get:
 * <p>
 * to provide means to access a particular key using a space separated path
 * <p>
 * supports named and indexed keys. Please, see the test package
 * <p>
 * <p>
 * <p>
 * Section Traversing Set:
 * <p>
 * to provide means to alter a particular key using a space separated path
 * <p>
 * supports named and indexed keys. Please, see the test package
 * <p>
 * <p>
 */
public class JsonMap<K, V> extends HashMap<K, V> {

  // Constructors
  public JsonMap() {
  }

  public JsonMap(Map entrySet) {
    super(entrySet);
  }

  public JsonMap(Object key, Object value) {
    addProp(key, value);
  }

  // Builders
  public static JsonMap from(Object target) {
    try {
      return fromJson(new ObjectMapper().writeValueAsString(target));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonMap fromJson(InputStream inputStream) {
    try {
      return fromJson(new String(IOUtils.toByteArray(inputStream)));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonMap fromJson(String json) {
    try {
      return new ObjectMapper().readValue(json, JsonMap.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonMap[] fromJsonArray(String json) {
    try {
      return new ObjectMapper().readValue(json, JsonMap[].class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static JsonMap[] fromJsonArray(String json, String key) throws Exception {
    JsonMap[] trv = fromJsonArray(json);
    Arrays.sort((JsonMap<String, String>[]) trv, new Comparator<JsonMap<String, String>>() {
      public int compare(JsonMap<String, String> j1, JsonMap<String, String> j2) {
        return j1.get(key).compareTo(j2.get(key));
      }
    });
    return trv;
  }

  public static String asJson(Object key, Object value) {
    return new JsonMap(key, value).asJson();
  }

  // Instance protocol
  public String asJson() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public JsonMap addProp(Object key, Object value) {
    put((K) key, (V) value);
    return this;
  }

  public JsonMap appendProp(Object key, Object value) {
    Vector val = (Vector) get(key);
    if (val == null) {
      put((K) key, (V) new Vector());
    }
    ((Vector) get(key)).add(value);
    return this;
  }

  public JsonMap addProps(Map adquiProps) {
    putAll(adquiProps);
    return this;
  }

  public JsonMap removeProp(String key) {
    remove(key);
    return this;
  }

  // Traversing: get
  public Object path(String path) {
    return pathOn(Arrays.asList(path.split(" ")).iterator());
  }

  public Object pathOn(Iterator<String> iterator) {
    String key = iterator.next();
    return pathOn(key, get(key), iterator, null);
  }

  protected Object pathIndexOn(List list, Iterator<String> iterator) {
    String key = iterator.next();
    return pathOn(key, list.get(Integer.parseInt(key)), iterator, list);
  }

  protected Object pathOn(String key, Object present, Iterator<String> iterator, List parent) {
    if (!iterator.hasNext()) {
      if (List.class.isInstance(present)) { // may be returning a no JsonMap list

        if (!List.class.cast(present).isEmpty() && Map.class.isInstance(List.class.cast(present).get(0))) {
          List<JsonMap> jt = new Vector();
          for (Map map : (List<Map>) present) {
            jt.add(new JsonMap(map));
          }
          put((K) key, (V) jt);
          return get(key);
        }

        return present;
      }
      if (Map.class.isInstance(present)) {
        // Logger.debug( this, "fixing JsonMap" );
        JsonMap jt = new JsonMap(Map.class.cast(present));
        if (List.class.isInstance(parent)) {
          parent.set(Integer.parseInt(key), (V) jt);
        } else {
          put((K) key, (V) jt);
        }
        return jt;
      }
      return present;
    }

    if (JsonMap.class.isInstance(present)) {
      return JsonMap.class.cast(present).pathOn(iterator);
    }

    if (Map.class.isInstance(present)) {
      // Logger.debug( this, "fixing JsonMap" );
      JsonMap jt = new JsonMap(Map.class.cast(present));
      if (List.class.isInstance(parent)) {
        parent.set(Integer.parseInt(key), (V) jt);
      } else {
        put((K) key, (V) jt);
      }
      return jt.pathOn(iterator);

    }

    if (List.class.isInstance(present)) {
      return pathIndexOn(List.class.cast(present), iterator);
    }

    return null;
  }

  // Traversing: set
  public JsonMap setPath(Object value, String path) {
    path(path); // to ensure the hole path is JsonMap'ed
    setPathOn(value, Arrays.asList(path.split(" ")).iterator());
    return this;
  }

  public void setPathOn(Object value, Iterator<String> iterator) {
    setPathOn(value, iterator.next(), iterator);
  }

  public void setPathOn(Object value, String key, Iterator<String> iterator) {
    if (!iterator.hasNext()) {
      addProp(key, value);
      return;
    }

    if (JsonMap.class.isInstance(get(key))) {
      JsonMap.class.cast(get(key)).setPathOn(value, iterator.next(), iterator);
      return;
    }

    if (List.class.isInstance(get(key))) {
      setPathIndexOn(value, Integer.parseInt(iterator.next()), List.class.cast(get(key)), iterator);
      return;
    }

  }

  protected void setPathIndexOn(Object value, int key, List list, Iterator<String> iterator) {

    if (!iterator.hasNext()) {
      list.set(key, value);
      return;
    }

    if (JsonMap.class.isInstance(list.get(key))) {
      JsonMap.class.cast(list.get(key)).setPathOn(value, iterator.next(), iterator);
      return;
    }

    if (List.class.isInstance(list.get(key))) {
      setPathIndexOn(value, Integer.parseInt(iterator.next()), List.class.cast(list.get(key)), iterator);
      return;
    }
  }

  public <T> T as(Class<T> clazz) {
    try {
      return new ObjectMapper().readValue(asJson(), clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
