package com.ensolvers.fox.services.queue;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashSet;
import java.util.Set;

/**
 * The Connector is responsible to call the queue handlers. Examples can be found in fox-sqs and fox-cache.
 *
 * @param <T> the type of object to be sent in the messages
 */

public class QueueConnector<T> {
  private final QueueAccessible<T> queueAccessible;
  private final Set<QueueListener<T>> listeners;
  private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

  public QueueConnector(QueueAccessible<T> queueAccessible, int numberOfThreadsToUse) {
    this.queueAccessible = queueAccessible;
    listeners = new HashSet<>();

    executor.setMaxPoolSize(numberOfThreadsToUse);
    executor.setQueueCapacity(0);
    executor.setThreadNamePrefix(queueAccessible.getLoggerPrefix());
    executor.initialize();

    for (int i = 0; i < numberOfThreadsToUse; i++) {
      executor.execute(() -> {
        while (true) {
          queueAccessible.consumeMessages(listeners);
        }
      });
    }
  }

  // registers a new listener
  public void addListener(QueueListener listener) {
    listeners.add(listener);
  }

  // removes an existing listener
  public void removeListener(QueueListener listener) {
    listeners.remove(listener);
  }

}
