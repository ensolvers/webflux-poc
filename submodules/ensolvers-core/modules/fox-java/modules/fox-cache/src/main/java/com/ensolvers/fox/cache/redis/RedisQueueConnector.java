package com.ensolvers.fox.cache.redis;

import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.queue.QueueAccessible;
import com.ensolvers.fox.services.queue.QueueListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A Queue Handler based on Redis using Redisson under it. This entity has to be injected into a QueueConnector using
 * the same type of T to process the messages.
 *
 * @param <T>
 */

public class RedisQueueConnector<T> implements QueueAccessible<T> {

  private static final Logger LOGGER = CoreLogger.getLogger(RedisQueueConnector.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final TypeReference<T> typeReference;

  private final RBlockingQueue<T> queue;

  public RedisQueueConnector(TypeReference<T> typeReference, String queueName, String redisUrl) {
    this.typeReference = typeReference;

    Config config = new Config();
    config.useSingleServer().setAddress(redisUrl);
    RedissonClient client = Redisson.create(config);

    queue = client.getBlockingQueue(queueName);
  }

  @Override
  public void publishMessage(T message) {
    queue.add(message);
  }

  @Override
  public void consumeMessages(Set<QueueListener<T>> listeners) {
    try {
      Optional<T> messageOptional = Optional.ofNullable(queue.poll(1, TimeUnit.SECONDS));

      if (messageOptional.isPresent()) {
        for (QueueListener<T> listener : listeners) {
          listener.onMessageReceived(messageOptional.get());
        }
      }
    } catch (InterruptedException ie) {
      LOGGER.error("[CRITICAL] A queue thread has been interrupted: ", ie);
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      LOGGER.error("An exception occurred while processing message: ", e);
    }
  }

  @Override
  public String serialize(T object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.error("[REDIS-Consumer] Error processing {}", object.toString());
      //TODO: decide a controlled output for errors in deserialization. An alternative could be to implement
      //TODO: the return as an Optional and just ignore wrong serializations.
      throw new RuntimeException();
    }
  }

  @Override
  public T deserialize(String representationOfT) {
    return OBJECT_MAPPER.convertValue(representationOfT, this.typeReference);
  }

  @Override
  public String getLoggerPrefix() {
    return "REDIS-CONSUMER-";
  }
}
