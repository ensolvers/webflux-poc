package com.ensolvers.fox.services.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Standard class that uses Jackson default configuration for
 * serializing objects
 */
public class JSONSerializer {

  private static JSONSerializer instance;
  private final ObjectMapper objectMapper;

  private JSONSerializer() {
    this.objectMapper = new ObjectMapper();
  }

  public static JSONSerializer get() {
    if (instance == null) {
      instance = new JSONSerializer();
    }

    return instance;
  }

  public <V> String serialize(V value) throws JsonProcessingException {
    return this.objectMapper.writeValueAsString(value);
  }

  public <V> V deserialize(String serializedObject, Class<V> type) throws JsonProcessingException {
    return this.objectMapper.readValue(serializedObject, type);
  }

  public <V1, V2> V1 deserialize(String serializedObject, Class<V1> externalType, Class<V2> internalType) throws JsonProcessingException {
    JavaType javaType = this.objectMapper.getTypeFactory().constructParametricType(externalType, internalType);
    return this.objectMapper.readValue(serializedObject, javaType);
  }

  public <V> V deserialize(String serializedObject, TypeReference<V> type) throws JsonProcessingException {
    return this.objectMapper.readValue(serializedObject, type);
  }

  public <V> V deserialize(String serializedObject, JavaType javaType) throws JsonProcessingException {
    return this.objectMapper.readValue(serializedObject, javaType);
  }

  public <V> List<V> deserializeList(List<String> serializedObjects, Class<V> type) throws JsonProcessingException {
    ArrayList<V> objects = new ArrayList<>(serializedObjects.size());

    for (String serializedObject : serializedObjects) {
      objects.add(this.deserialize(serializedObject, type));
    }

    return objects;
  }

  public <V> List<V> serializeList(Collection<V> objects) throws JsonProcessingException {
    List<V> serializedObjects = new ArrayList<>(objects.size());

    for (Object object : objects) {
      serializedObjects.add((V) this.serialize(object));
    }

    return serializedObjects;
  }

  public <K, V> Map<K, String> serializeMapValues(Map<K, V> objectMap) throws JsonProcessingException {
    Map<K, String> serializedObjectMap = new HashMap<>(objectMap.size());

    for (Map.Entry<K, V> entry : objectMap.entrySet()) {
      serializedObjectMap.put(entry.getKey(), this.serialize(entry.getValue()));
    }

    return serializedObjectMap;
  }

  public <K, V> Map<K, V> deserializeMapValues(Map<K, String> serializedObjectMap, Class<V> type) throws JsonProcessingException {
    Map<K, V> objectMap = new HashMap<>(serializedObjectMap.size());

    for (Map.Entry<K, String> entry : serializedObjectMap.entrySet()) {
      objectMap.put(entry.getKey(), (V) this.deserialize(entry.getValue(), type));
    }

    return objectMap;
  }

  public static JavaType listType(Class<?> type) {
    return get().objectMapper.getTypeFactory().constructParametricType(List.class, type);
  }

  public static JavaType type(Class<?> type) {
    return get().objectMapper.getTypeFactory().constructType(type);
  }

  public static JavaType parameterizedType(Class<?> wrapperType, JavaType internalType) {
    return get().objectMapper.getTypeFactory().constructParametricType(wrapperType, internalType);
  }

}
