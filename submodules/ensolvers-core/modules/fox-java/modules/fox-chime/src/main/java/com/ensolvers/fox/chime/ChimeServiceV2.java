package com.ensolvers.fox.chime;

import com.ensolvers.fox.services.logging.CoreLogger;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.auth.signer.internal.BaseAws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.awscore.AwsClient;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.chimesdkidentity.ChimeSdkIdentityClient;
import software.amazon.awssdk.services.chimesdkidentity.ChimeSdkIdentityClientBuilder;
import software.amazon.awssdk.services.chimesdkidentity.model.*;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClient;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClientBuilder;
import software.amazon.awssdk.services.chimesdkmeetings.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.chimesdkmeetings.model.NotFoundException;
import software.amazon.awssdk.services.chimesdkmessaging.ChimeSdkMessagingClient;
import software.amazon.awssdk.services.chimesdkmessaging.ChimeSdkMessagingClientBuilder;
import software.amazon.awssdk.services.chimesdkmessaging.model.*;
import software.amazon.awssdk.services.chimesdkmessaging.model.Identity;

import java.net.URI;
import java.time.Instant;
import java.util.*;

public class ChimeServiceV2 {

  private static final CoreLogger logger = CoreLogger.getLogger(ChimeServiceV2.class);
  private final ChimeSdkMeetingsClient meetingsClient;
  private final ChimeSdkIdentityClient identityClient;
  private final ChimeSdkMessagingClient messagingClient;
  private final AwsCredentials awsCredentials;
  private final String appInstanceArn;
  private final Region region;

  /**
   * Constructs a Chime Service
   *
   * @param managementCredentials the AWS credentials for a user with read/write access to all chime services
   * @param region                the AWS region
   * @param appInstanceArn        the app instance arn where the chat channels will be scoped
   */
  public ChimeServiceV2(AwsBasicCredentials managementCredentials, Region region, String appInstanceArn) {
    this.appInstanceArn = appInstanceArn;
    this.region = region;
    this.awsCredentials = managementCredentials;

    this.meetingsClient = ChimeSdkMeetingsClient.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(managementCredentials)).build();
    this.identityClient = ChimeSdkIdentityClient.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(managementCredentials)).build();
    this.messagingClient = ChimeSdkMessagingClient.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(managementCredentials))
        .build();
  }

  public Meeting getMeeting(String meetingId) {
    GetMeetingRequest request = GetMeetingRequest.builder().meetingId(meetingId).build();

    return meetingsClient.getMeeting(request).meeting();
  }

  /**
   * Creates a video meeting
   *
   * @param clientRequestToken a unique identifier
   * @return the meeting
   */
  public Meeting createMeeting(String clientRequestToken) {
    CreateMeetingRequest request = CreateMeetingRequest.builder().clientRequestToken(clientRequestToken).externalMeetingId(clientRequestToken)
        .mediaRegion(region.toString()).build();

    Meeting meeting = meetingsClient.createMeeting(request).meeting();
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
    CreateAttendeeRequest request = CreateAttendeeRequest.builder().meetingId(meetingId).externalUserId(userId).build();
    Attendee attendee = meetingsClient.createAttendee(request).attendee();
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
    CreateAppInstanceUserRequest request = CreateAppInstanceUserRequest.builder().appInstanceArn(appInstanceArn).name(fullName).metadata(metadata)
        .appInstanceUserId(userId).build();

    CreateAppInstanceUserResponse appInstanceUser = identityClient.createAppInstanceUser(request);
    logger.info("User created for userId {}, arn: {}", userId, appInstanceUser);
    return appInstanceUser.appInstanceUserArn();
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
    UpdateAppInstanceUserRequest request = UpdateAppInstanceUserRequest.builder().appInstanceUserArn(userArn).name(fullName).metadata(metadata).build();

    UpdateAppInstanceUserResponse appInstanceUser = identityClient.updateAppInstanceUser(request);
    logger.info("User with arn: {} updated", userArn);
    return appInstanceUser.appInstanceUserArn();
  }

  public String updateUser(String userArn, String fullName) {
    UpdateAppInstanceUserRequest request = UpdateAppInstanceUserRequest.builder().appInstanceUserArn(userArn).name(fullName).build();

    UpdateAppInstanceUserResponse appInstanceUser = identityClient.updateAppInstanceUser(request);
    logger.info("User with arn: {} updated", userArn);
    return appInstanceUser.appInstanceUserArn();
  }

  public void addMembersToChannel(String channelArn, String channelCreatorArn, Collection<String> memberArns) {
    BatchCreateChannelMembershipRequest request = BatchCreateChannelMembershipRequest.builder().channelArn(channelArn).chimeBearer(channelCreatorArn)
        .memberArns(memberArns).type(ChannelMembershipType.DEFAULT).build();

    List<Identity> members = this.messagingClient.batchCreateChannelMembership(request).batchChannelMemberships().members();
    logger.info("Members added to channel: {}", members);
  }

  public String createChannel(String name, String creatorArn, String metadata) {
    CreateChannelRequest createChannelRequest = CreateChannelRequest.builder().appInstanceArn(appInstanceArn).chimeBearer(creatorArn).name(name)
        .metadata(metadata).privacy(ChannelPrivacy.PRIVATE).build();

    String arn = messagingClient.createChannel(createChannelRequest).channelArn();
    logger.info("Channel created: {}", arn);
    return arn;
  }

  public String createChannel(String name, String creatorArn) {
    return this.createChannel(name, creatorArn, null);
  }

  public String updateChannel(String channelArn, String creatorArn, String name, String metadata) {
    UpdateChannelRequest updateChannelRequest = UpdateChannelRequest.builder().chimeBearer(creatorArn).channelArn(channelArn).name(name).metadata(metadata)
        .build();

    UpdateChannelResponse updateChannelResult = messagingClient.updateChannel(updateChannelRequest);
    logger.info("Channel with arn: {} updated", channelArn);
    return updateChannelResult.channelArn();
  }

  public String sendMessage(String userArn, String channelArn, String message, String metadata) {
    return this.sendMessage(userArn, channelArn, message, metadata, ChannelMessageType.STANDARD);
  }

  public String sendMessage(String userArn, String channelArn, String message, String metadata, ChannelMessageType type) {
    SendChannelMessageRequest sendChannelMessageRequest = SendChannelMessageRequest.builder().channelArn(channelArn).chimeBearer(userArn).type(type)
        .persistence(ChannelMessagePersistenceType.PERSISTENT).content(message).metadata(metadata).build();

    return this.messagingClient.sendChannelMessage(sendChannelMessageRequest).messageId();
  }

  public ListChannelsResponse listChannels(String userArn) {
    ListChannelsRequest request = ListChannelsRequest.builder().chimeBearer(userArn).appInstanceArn(appInstanceArn).build();

    return this.messagingClient.listChannels(request);
  }

  public ListChannelMessagesResponse listMessages(String userArn, String channelArn, Integer maxResults, String cursor, Instant beforeDate, Instant afterDate) {
    ListChannelMessagesRequest.Builder builder = ListChannelMessagesRequest.builder().channelArn(channelArn).chimeBearer(userArn).maxResults(maxResults);

    if (beforeDate != null) {
      builder.notAfter(beforeDate);
    }
    if (afterDate != null) {
      builder.notBefore(afterDate);
    }
    if (StringUtils.hasLength(cursor)) {
      builder.nextToken(cursor);
    }

    return this.messagingClient.listChannelMessages(builder.build());
  }

  /**
   * This method creates an app instance where a set of users and channels will exist
   *
   * @param name the name of the app instance
   * @return the app instance arn
   */
  public String createAppInstance(String name) {
    CreateAppInstanceRequest createAppInstanceRequest = CreateAppInstanceRequest.builder().name(name).build();
    CreateAppInstanceResponse appInstance = this.identityClient.createAppInstance(createAppInstanceRequest);

    String newAppInstanceArn = appInstance.appInstanceArn();
    logger.info("App instance created: {}", newAppInstanceArn);
    return newAppInstanceArn;
  }

  private String getMessagingSessionUrl() {
    GetMessagingSessionEndpointRequest request = GetMessagingSessionEndpointRequest.builder().build();
    GetMessagingSessionEndpointResponse result = this.messagingClient.getMessagingSessionEndpoint(request);

    return result.endpoint().url();
  }

  /**
   * Generates a websocket URL to open a wss connection to receive real-time Chime messages for a specific user
   *
   * @param appInstanceUserArn the user arn of the user
   * @param expirationTime       the date when the websocket will stop being valid
   * @return a wss url to connect
   */
  public String getWebSocketConnection(String appInstanceUserArn, Instant expirationTime) {
    // We create the URI with https instead of wss because SdkHttpRequest does not support it.
    URI uri = URI.create("https://" + this.getMessagingSessionUrl() + "/connect");

    SdkHttpFullRequest request = SdkHttpFullRequest.builder().uri(uri).method(SdkHttpMethod.GET)
        .putRawQueryParameter("userArn", Collections.singletonList(appInstanceUserArn))
        .putRawQueryParameter("sessionId", Collections.singletonList(UUID.randomUUID().toString())).build();

    Aws4PresignerParams params = Aws4PresignerParams.builder().signingName("chime").expirationTime(expirationTime).signingRegion(this.region)
        .awsCredentials(this.awsCredentials).build();

    SdkHttpFullRequest signedRequest = Aws4Signer.create().presign(request, params);
    return signedRequest.getUri().toString().replace("https", "wss");
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
    GetChannelMessageRequest getChannelMessageRequest = GetChannelMessageRequest.builder().channelArn(channelArn).messageId(messageId).chimeBearer(chimeBearer)
        .build();

    GetChannelMessageResponse channelMessage = messagingClient.getChannelMessage(getChannelMessageRequest);

    return channelMessage.channelMessage();
  }

  /**
   * Deleted a message from a given channel, only for admins
   *
   * @param chimeBearer The chime bearer
   * @param channelArn  The channel arn
   * @param messageId   The id of the message to be deleted
   */
  public void deleteChannelMessage(String chimeBearer, String channelArn, String messageId) {
    DeleteChannelMessageRequest deleteChannelMessageRequest = DeleteChannelMessageRequest.builder().channelArn(channelArn).messageId(messageId)
        .chimeBearer(chimeBearer).build();

    messagingClient.deleteChannelMessage(deleteChannelMessageRequest);
  }
}
