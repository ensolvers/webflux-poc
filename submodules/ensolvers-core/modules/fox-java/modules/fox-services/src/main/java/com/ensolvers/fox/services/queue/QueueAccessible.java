package com.ensolvers.fox.services.queue;

import java.util.Set;

public interface QueueAccessible<T> {

  void publishMessage(T message);

  void consumeMessages(Set<QueueListener<T>> listeners);

  String serialize(T object);

  T deserialize(String representationOfT);

  String getLoggerPrefix();
}
