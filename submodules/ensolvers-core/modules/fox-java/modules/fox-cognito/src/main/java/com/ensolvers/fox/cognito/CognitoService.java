package com.ensolvers.fox.cognito;

import java.util.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

public class CognitoService {
  private final String userPoolId;
  private final String clientId;
  private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

  public CognitoService(String userPoolId, String clientId, String accessKey, String secretKey) {
    this.userPoolId = userPoolId;
    this.clientId = clientId;

    var awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

    this.cognitoIdentityProviderClient = CognitoIdentityProviderClient.builder().credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
        .region(Region.US_EAST_1).build();
  }

  /**
   * Add a user to a cognito group, the group and the user must exist in cognito or else it will fail
   *
   * @param username The user's username or email
   * @param group    The previously defined group in cognito
   * @return An object with all the response data
   */
  public AdminAddUserToGroupResponse addUserToGroup(String username, String group) {
    var adminAddUserToGroupRequest = AdminAddUserToGroupRequest.builder().userPoolId(userPoolId).username(username).groupName(group).build();

    return cognitoIdentityProviderClient.adminAddUserToGroup(adminAddUserToGroupRequest);
  }

  /**
   * Create a user with some password, this method is used for pools that use username/password authentication
   *
   * @param username         The user's username or email
   * @param password         The password, the conditions for a password are defined in the user pool
   * @param sendConfirmation True if you want the user to receive a confirmation mail
   * @return An object with all the response data
   */
  public AdminCreateUserResponse createUserWithPassword(String username, String password, boolean sendConfirmation) {
    var request = AdminCreateUserRequest.builder().username(username).userPoolId(userPoolId).temporaryPassword(password);

    if (!sendConfirmation) {
      request.messageAction(MessageActionType.SUPPRESS);
    }

    return cognitoIdentityProviderClient.adminCreateUser(request.build());
  }

  /**
   * Sign in, this method is used for pools that use username/password authentication
   *
   * @param username The user's username or email
   * @param password The user's password
   * @return An object with all the response data, this object contains the access, id and refresh tokens, if the user
   *         hasn't changed their password, this will return a challenge session
   */
  public AdminInitiateAuthResponse signInWithPassword(String username, String password) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", username);
    authParams.put("PASSWORD", password);

    var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder().authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH).authParameters(authParams)
        .userPoolId(userPoolId).clientId(clientId).build();

    return cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
  }

  /**
   * Changed password of a user, this method needs to be called to be able to sign in with pools that have a
   * force_change_password policy
   *
   * @param username         The user's username or email
   * @param newPassword      The user's new password
   * @param challengeSession The session received by the sign in method
   * @return An object with all the response data
   */
  public AdminRespondToAuthChallengeResponse changePassword(String username, String newPassword, String challengeSession) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", username);
    authParams.put("NEW_PASSWORD", newPassword);

    var adminRespondToAuthChallengeRequest = AdminRespondToAuthChallengeRequest.builder().challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
        .challengeResponses(authParams).userPoolId(userPoolId).session(challengeSession).clientId(clientId).build();

    return cognitoIdentityProviderClient.adminRespondToAuthChallenge(adminRespondToAuthChallengeRequest);
  }

  /**
   * Changes a password user
   *
   * @param username    the username or email
   * @param newPassword the new password
   * @return the response object without any information
   */
  public AdminSetUserPasswordResponse resetPassword(String username, String newPassword) {
    var adminSetUserPasswordRequest = AdminSetUserPasswordRequest.builder().username(username).password(newPassword).userPoolId(userPoolId).permanent(true)
        .build();

    return cognitoIdentityProviderClient.adminSetUserPassword(adminSetUserPasswordRequest);
  }

  /**
   * Refresh a user token, when an id token expires, this method should be called to refresh it
   *
   * @param refreshToken The user's refresh token
   * @return An object containing id and access tokens
   */
  public AdminInitiateAuthResponse refreshToken(String refreshToken) {
    Map<String, String> authParams = new HashMap<>();
    authParams.put("REFRESH_TOKEN", refreshToken);

    var adminInitiateAuthRequest = AdminInitiateAuthRequest.builder().authFlow(AuthFlowType.REFRESH_TOKEN_AUTH).authParameters(authParams)
        .userPoolId(userPoolId).clientId(clientId).build();

    return cognitoIdentityProviderClient.adminInitiateAuth(adminInitiateAuthRequest);
  }

  /**
   * Method to delete a user in cognito, if the username exists it deletes it, no matter if caller is not that user
   *
   * @param username User's username or email (depending on cognito pool configuration)
   * @return An AdminDeleteUserResponse containing success or error state
   */
  public AdminDeleteUserResponse deleteUser(String username) {
    AdminDeleteUserRequest adminDeleteUserRequest = AdminDeleteUserRequest.builder().username(username).userPoolId(userPoolId).build();

    return cognitoIdentityProviderClient.adminDeleteUser(adminDeleteUserRequest);
  }

  /**
   * Method to get a user in cognito, if the username exist it gets it, but if not exists throw the UserNotFoundException
   * 
   * @param username
   * @return
   */
  public AdminGetUserResponse getUser(String username) {
    AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder().username(username).userPoolId(userPoolId).build();
    return cognitoIdentityProviderClient.adminGetUser(adminGetUserRequest);
  }
}
