package com.ensolvers.fox.kafka;

import com.ensolvers.fox.services.queue.QueueAccessible;
import com.ensolvers.fox.services.queue.QueueListener;

import java.util.Set;

public class KafkaQueueConnector<T> implements QueueAccessible<T> {

  @Override
  public void publishMessage(T message) {

  }

  @Override
  public void consumeMessages(Set<QueueListener<T>> listeners) {

  }

  @Override
  public String serialize(T object) {
    return null;
  }

  @Override
  public T deserialize(String representationOfT) {
    return null;
  }

  @Override
  public String getLoggerPrefix() {
    return null;
  }
}