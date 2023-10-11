package com.ensolvers.fox.services.queue;

public interface QueueListener<T> {

  void onMessageReceived(T object);
}
