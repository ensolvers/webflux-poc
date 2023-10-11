/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.chime;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.chime.AmazonChime;
import com.amazonaws.services.chime.AmazonChimeClient;
import com.amazonaws.services.chime.model.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * The Chime service is used to create/join a videoconference meeting and also to send/list text channels and messages
 *
 * @author Facundo Garbino
 */
public class ChimeService {
  private static final Logger logger = LoggerFactory.getLogger(ChimeService.class);
  private final AmazonChime amazonChime;
  private final String appInstanceArn;
  private final BasicAWSCredentials chattingCredentials;
  private final Regions region;

  /**
   * Constructs a Chime Service
   *
   * @param managementCredentials the AWS credentials for a user with read/write access to all chime services
   * @param region                the AWS region
   * @param appInstanceArn        the app instance arn where the chat channels will be scoped
   * @param chattingCredentials   the AWS credentials for a user with permission to chime:connect
   */
  public ChimeService(BasicAWSCredentials managementCredentials, Regions region, String appInstanceArn, BasicAWSCredentials chattingCredentials) {
    this.appInstanceArn = appInstanceArn;
    this.amazonChime = AmazonChimeClient.builder().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(managementCredentials)).build();
    this.chattingCredentials = chattingCredentials;
    this.region = region;
  }

  public Meeting getMeeting(String meetingId) {
    GetMeetingRequest request = new GetMeetingRequest();
    request.setMeetingId(meetingId);

    return amazonChime.getMeeting(request).getMeeting();
  }

  /**
   * Creates a video meeting
   *
   * @param clientRequestToken a unique identifier
   * @return the meeting
   */
  public Meeting createMeeting(String clientRequestToken) {
    CreateMeetingRequest request = new CreateMeetingRequest();
    request.setClientRequestToken(clientRequestToken);
    Meeting meeting = amazonChime.createMeeting(request).getMeeting();
    logger.info("Meeting created: {}", meeting);

    return meeting;
  }

  /**
   * Creates an attendee object with the credentials to join a meeting with the specified user id
   *
   * @param userId    the user id
   * @param meetingId the (already created) meeting id
   * @return the attendee object with credentials
   * @throws NotFoundException if the meeting wasn't found
   */
  public Attendee joinMeeting(String userId, String meetingId) throws NotFoundException {
    CreateAttendeeRequest request = new CreateAttendeeRequest();
    request.setMeetingId(meetingId);
    request.setExternalUserId(userId);
    Attendee attendee = amazonChime.createAttendee(request).getAttendee();
    logger.info("Attendee created: {}", attendee);

    return attendee;
  }

  /**
   * Creates an app instance user inside the app instance to be able to be member of channels
   *
   * @param userId   the user id
   * @param fullName the display name
   * @param metadata the user metadata
   * @return the app instance user arn
   */
  public String createUser(String userId, String fullName, String metadata) {
    CreateAppInstanceUserRequest userRequest = new CreateAppInstanceUserRequest().withAppInstanceArn(appInstanceArn).withName(fullName).withMetadata(metadata)
        .withAppInstanceUserId(userId);

    CreateAppInstanceUserResult appInstanceUser = amazonChime.createAppInstanceUser(userRequest);
    logger.info("User created for userId {}, arn: {}", userId, appInstanceUser);
    return appInstanceUser.getAppInstanceUserArn();
  }

  /**
   * Creates an app instance user inside the app instance to be able to be member of channels
   *
   * @param userId   the user id
   * @param fullName the display name
   * @return the app instance user arn
   */
  public String createUser(String userId, String fullName) {
    return this.createUser(userId, fullName, null);
  }

  public String updateUser(String userArn, String fullName, String metadata) {
    UpdateAppInstanceUserRequest userRequest = new UpdateAppInstanceUserRequest().withAppInstanceUserArn(userArn).withName(fullName).withMetadata(metadata);

    UpdateAppInstanceUserResult appInstanceUser = amazonChime.updateAppInstanceUser(userRequest);
    logger.info("User with arn: {} updated", userArn);
    return appInstanceUser.getAppInstanceUserArn();
  }

  public String updateUser(String userArn, String fullName) {
    UpdateAppInstanceUserRequest userRequest = new UpdateAppInstanceUserRequest().withAppInstanceUserArn(userArn).withName(fullName);

    UpdateAppInstanceUserResult appInstanceUser = amazonChime.updateAppInstanceUser(userRequest);
    logger.info("User with arn: {} updated", userArn);
    return appInstanceUser.getAppInstanceUserArn();
  }

  public void addMembersToChannel(String channelArn, String channelCreatorArn, Collection<String> memberArns) {
    BatchCreateChannelMembershipRequest request = new BatchCreateChannelMembershipRequest().withChannelArn(channelArn).withChimeBearer(channelCreatorArn)
        .withMemberArns(memberArns).withType(ChannelMembershipType.DEFAULT);

    List<Identity> members = this.amazonChime.batchCreateChannelMembership(request).getBatchChannelMemberships().getMembers();
    logger.info("Members added to channel: {}", members);
  }

  public String createChannel(String name, String creatorArn, String metadata) {
    CreateChannelRequest createChannelRequest = new CreateChannelRequest().withAppInstanceArn(appInstanceArn).withChimeBearer(creatorArn).withName(name)
        .withMetadata(metadata).withPrivacy(ChannelPrivacy.PRIVATE);

    String arn = amazonChime.createChannel(createChannelRequest).getChannelArn();
    logger.info("Channel created: {}", arn);
    return arn;
  }

  public String createChannel(String name, String creatorArn) {
    return this.createChannel(name, creatorArn, null);
  }

  public String updateChannel(String channelArn, String creatorArn, String name, String metadata) {
    UpdateChannelRequest updateChannelRequest = new UpdateChannelRequest().withChimeBearer(creatorArn).withChannelArn(channelArn).withName(name)
        .withMetadata(metadata);

    UpdateChannelResult updateChannelResult = amazonChime.updateChannel(updateChannelRequest);
    logger.info("Channel with arn: {} updated", channelArn);
    return updateChannelResult.getChannelArn();
  }

  public String sendMessage(String userArn, String channelArn, String message, String metadata) {
    return this.sendMessage(userArn, channelArn, message, metadata, ChannelMessageType.STANDARD);
  }

  public String sendMessage(String userArn, String channelArn, String message, String metadata, ChannelMessageType type) {
    SendChannelMessageRequest sendChannelMessageRequest = new SendChannelMessageRequest().withChannelArn(channelArn).withChimeBearer(userArn).withType(type)
        .withPersistence(ChannelMessagePersistenceType.PERSISTENT).withContent(message).withMetadata(metadata);

    return this.amazonChime.sendChannelMessage(sendChannelMessageRequest).getMessageId();
  }

  public ListChannelsResult listChannels(String userArn) {
    ListChannelsRequest request = new ListChannelsRequest().withChimeBearer(userArn).withAppInstanceArn(appInstanceArn);

    return this.amazonChime.listChannels(request);
  }

  public ListChannelMessagesResult listMessages(String userArn, String channelArn, Integer maxResults, String cursor, Date beforeDate, Date afterDate) {
    ListChannelMessagesRequest listChannelMessagesRequest = new ListChannelMessagesRequest().withChannelArn(channelArn).withChimeBearer(userArn)
        .withMaxResults(maxResults);
    if (beforeDate != null) {
      listChannelMessagesRequest.withNotAfter(beforeDate);
    }
    if (afterDate != null) {
      listChannelMessagesRequest.withNotBefore(afterDate);
    }
    if (StringUtils.hasLength(cursor)) {
      listChannelMessagesRequest.setNextToken(cursor);
    }

    return this.amazonChime.listChannelMessages(listChannelMessagesRequest);
  }

  /**
   * This method creates an app instance where a set of users and channels will exist
   *
   * @param name the name of the app instance
   * @return the app instance arn
   */
  public String createAppInstance(String name) {
    CreateAppInstanceRequest createAppInstanceRequest = new CreateAppInstanceRequest().withName(name);
    CreateAppInstanceResult appInstance = this.amazonChime.createAppInstance(createAppInstanceRequest);
    String newAppInstanceArn = appInstance.getAppInstanceArn();
    logger.info("App instance created: {}", newAppInstanceArn);
    return newAppInstanceArn;
  }

  private String getMessagingSessionUrl() {
    GetMessagingSessionEndpointRequest request = new GetMessagingSessionEndpointRequest();
    GetMessagingSessionEndpointResult result = this.amazonChime.getMessagingSessionEndpoint(request);

    return result.getEndpoint().getUrl();
  }

  /**
   * Generates a websocket URL to open a wss connection to receive real-time Chime messages for a specific user
   *
   * @param appInstanceUserArn the user arn of the user
   * @param dateToExpire       the date when the websocket will stop being valid
   * @return a wss url to connect
   */
  public String getWebSocketConnection(String appInstanceUserArn, Date dateToExpire) {
    // Basic request (endpoint + params)
    Request<Void> request = new DefaultRequest<>("chime"); // Request to Chime
    request.setHttpMethod(HttpMethodName.GET);
    request.setEndpoint(URI.create("wss://" + this.getMessagingSessionUrl()));
    request.setResourcePath("connect");
    request
        .setParameters(Map.of("sessionId", Collections.singletonList(UUID.randomUUID().toString()), "userArn", Collections.singletonList(appInstanceUserArn)));

    // Presign the request with HMAC-SHA-256
    AWS4Signer signer = new AWS4Signer();
    signer.setRegionName(region.getName());
    signer.setServiceName("chime");
    signer.presignRequest(request, this.chattingCredentials, dateToExpire);

    // Generate URL appending parameters
    StringBuilder endpoint = new StringBuilder(request.getEndpoint().toString()).append("/").append(request.getResourcePath()).append("?");
    request.getParameters().forEach((k, v) -> {
      String value = v.get(0);
      // These two parameters need to be encoded since they have slashes and other
      // special
      // characters
      if (k.equals("X-Amz-Credential") || k.equals("userArn")) {
        value = URLEncoder.encode(value, StandardCharsets.UTF_8);
      }
      endpoint.append(k).append('=').append(value).append('&');
    });
    // Remove the final ampersand (&)
    return endpoint.substring(0, endpoint.length() - 1);
  }

  /**
   * Method to obtain a specific channel message by id
   *
   * @param chimeBearer The chime bearer
   * @param channelArn  The channel arn
   * @param messageId   The id of the message to be obtained
   * @return The message obtained by id as ChannelMessage
   */
  public ChannelMessage getChannelMessage(String chimeBearer, String channelArn, String messageId) {
    GetChannelMessageRequest getChannelMessageRequest = new GetChannelMessageRequest().withChannelArn(channelArn).withMessageId(messageId)
        .withChimeBearer(chimeBearer);
    GetChannelMessageResult channelMessage = amazonChime.getChannelMessage(getChannelMessageRequest);

    return channelMessage.getChannelMessage();
  }

  /**
   * Deleted a message from a given channel, only for admins
   *
   * @param chimeBearer The chime bearer
   * @param channelArn  The channel arn
   * @param messageId   The id of the message to be deleted
   */
  public void deleteChannelMessage(String chimeBearer, String channelArn, String messageId) {
    DeleteChannelMessageRequest deleteChannelMessageRequest = new DeleteChannelMessageRequest().withChannelArn(channelArn).withMessageId(messageId)
        .withChimeBearer(chimeBearer);

    amazonChime.deleteChannelMessage(deleteChannelMessageRequest);
  }
}
