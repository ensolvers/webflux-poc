package com.ensolvers.fox.sqs;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.ensolvers.core.api.configuration.ApplicationPropertiesService;
import com.ensolvers.fox.services.logging.CoreLogger;
import com.ensolvers.fox.services.queue.QueueAccessible;
import com.ensolvers.fox.services.queue.QueueListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class SQSQueueConnector<T> implements QueueAccessible<T> {

  @Autowired
  ApplicationPropertiesService applicationPropertiesService;

  private static final Logger LOGGER = CoreLogger.getLogger(SQSQueueConnector.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final TypeReference<T> typeReference;
  private final AmazonSQS sqs;
  private final String standardQueueUrl;

  public SQSQueueConnector(TypeReference<T> typeReference, String queueName) {
    this.typeReference = typeReference;

    sqs = AmazonSQSClientBuilder.standard().withCredentials(new EnvironmentVariableCredentialsProvider()).withRegion(Regions.US_EAST_1).build();

    CreateQueueRequest createStandardQueueRequest = new CreateQueueRequest(queueName);
    this.standardQueueUrl = sqs.createQueue(createStandardQueueRequest).getQueueUrl();
  }

  // to submit a new message to the queue
  @Override
  public void publishMessage(T message) {

    String objectAsJson;

    try {
      objectAsJson = OBJECT_MAPPER.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      LOGGER.error("[SQS Connector] Error publishing message");
      throw new RuntimeException();
    }

    LOGGER.info("[CORE-SQS] Publishing in SQS, Message: {}", objectAsJson);

    SendMessageRequest sendMessageStandardQueue = new SendMessageRequest().withQueueUrl(standardQueueUrl).withMessageBody(objectAsJson).withDelaySeconds(30);

    sqs.sendMessage(sendMessageStandardQueue);
  }

  @Override
  public void consumeMessages(Set<QueueListener<T>> listeners) {
    ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(this.standardQueueUrl).withWaitTimeSeconds(10).withVisibilityTimeout(10)
        .withMaxNumberOfMessages(10);

    ReceiveMessageResult response = this.sqs.receiveMessage(receiveMessageRequest);

    for (Message message : response.getMessages()) {
      if (this.deleteMessageFromQueue(message)) {
        T object = deserialize(message.getBody());
        for (QueueListener<T> listener : listeners) {
          listener.onMessageReceived(object);
        }
      }
    }
  }

  @Override
  public String serialize(T object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      LOGGER.error("[SQS-QUEUE] Error processing {}", object.toString());
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
    return "SQS-CONSUMER-";
  }

  public boolean deleteMessageFromQueue(Message message) {
    try {
      this.sqs.deleteMessage(this.standardQueueUrl, message.getReceiptHandle());
      return true;
    } catch (Exception e) {
      LOGGER.error("Error trying to delete message from SQS: " + message.getBody(), e);
      return false;
    }
  }
}
