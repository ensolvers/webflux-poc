package com.ensolvers.fox.alerts;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Attachment;

import java.io.IOException;
import java.io.File;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slack.api.methods.response.files.FilesUploadResponse;

/**
 * Provides several utilities and shortcut methods for sending messages to Slack channels.
 */
public class SlackService {

  Logger logger = LoggerFactory.getLogger(SlackService.class);

  private final Slack instance;
  private final String token;
  private final String defaultChannel;

  /**
   * Creates an instance with a specific bot token and default channel
   *
   * @param token          bot token that will be used to send messages - an app must be created and installed an da bot
   *                       token should be generated and provided
   * @param defaultChannel for methods that do not require a channel, the channel id that will be used to send messages
   */
  public SlackService(String token, String defaultChannel) {
    this.instance = Slack.getInstance();
    this.token = token;
    this.defaultChannel = defaultChannel;
  }

  /**
   * Sends a simple message to a channel
   *
   * @param channelId the id of the channel
   * @param message   the message to be sent
   * @return the response from Slack API
   * @throws Exception if an error or unexpected situation occurs when trying to send the message
   */
  public ChatPostMessageResponse sendMessage(String channelId, String message) throws SlackApiException, IOException {
    return this.instance.methods(this.token).chatPostMessage(req -> req.channel(channelId).mrkdwn(true).text(message));
  }

  /**
   * Sends a message inside a block with a customized color
   *
   * @param message the message to be sent
   * @param color   the color of the block in hex format - e.g. `{@code #FF0000} for pure red
   * @return the response from Slack API
   * @throws Exception if an error or unexpected situation occurs when trying to send the message
   */
  public ChatPostMessageResponse sendMessageWithColor(String message, String color) throws SlackApiException, IOException {
    return this.instance.methods(this.token).chatPostMessage(
        req -> req.channel(this.defaultChannel).attachments(Collections.singletonList(Attachment.builder().color(color).text(message).build())));
  }

  /**
   * Sends a simple message to the default channel
   *
   * @param message the message to be sent
   * @return the response from Slack API
   * @throws Exception if an error or unexpected situation occurs when trying to send the message
   */
  public ChatPostMessageResponse sendMessageToDefaultChannel(String message) throws SlackApiException, IOException {
    return this.sendMessage(this.defaultChannel, message);
  }

  /**
   * Sends a message with an attachment to the given channel
   *
   * @param channelId the id of the channel
   * @param message the message to be sent
   * @param attachment the attachment
   * @param fileName the name of the file
   * @return the response from Slack API
   * @throws Exception if an error or unexpected situation occurs when trying to send the message
   */
  public FilesUploadResponse sendMessageWithAttachment(String channelId, String message, File attachment, String fileName)
      throws SlackApiException, IOException {
    return this.instance.methods(this.token)
        .filesUpload(req -> req.channels(Collections.singletonList(channelId)).file(attachment).filename(fileName).initialComment(message));
  }

}
