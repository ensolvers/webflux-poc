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
package com.ensolvers.fox.ses;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The SES service takes care of sending emails using AWS SES
 *
 * @author Facundo Garbino
 */
public class SESService {

  private static final Logger logger = LoggerFactory.getLogger(SESService.class);
  private static final String LOG_PREFIX = "[AWS-SES]";
  private static final String UTF_8 = "UTF-8";

  private final AmazonSimpleEmailService client;

  public SESService(AmazonSimpleEmailService client) {
    this.client = client;
  }

  /**
   * Sends an email with the specified parameters
   *
   * @param fromEmail the email to sent it from (must be from a validated domain)
   * @param subject   the email subject
   * @param bodyText  the email body (might be plain text or HTML)
   * @param isHTML    whether the email is HTML or plain text
   * @param toEmails  an array of email addresses to send the email to
   * @return the message id of the result
   */
  public String sendEmail(String fromEmail, String subject, String bodyText, boolean isHTML, String... toEmails) {
    return sendEmail(fromEmail, subject, null, bodyText, isHTML, toEmails);
  }

  public String sendEmail(String fromEmail, String subject, String configurationSet, String bodyText, boolean isHTML, String... toEmails) {
    Body body = new Body();
    if (isHTML) {
      body.withHtml(new Content().withCharset(UTF_8).withData(bodyText));
    } else {
      body.withText(new Content().withCharset(UTF_8).withData(bodyText));
    }

    SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(toEmails))
        .withMessage(new com.amazonaws.services.simpleemail.model.Message().withBody(body).withSubject(new Content().withCharset(UTF_8).withData(subject)))
        .withSource(fromEmail);

    if (configurationSet != null) {
      request.withConfigurationSetName(configurationSet);
    }

    SendEmailResult sendEmailResult = client.sendEmail(request);
    String emailList = Arrays.toString(toEmails);
    logger.info("{} Email sent to {}, result: {}", LOG_PREFIX, emailList, sendEmailResult);
    return sendEmailResult.getMessageId();
  }

  /**
   * Sends an email with the specified parameters
   *
   * @param fromEmail   the email to sent it from (must be from a validated domain)
   * @param subject     the email subject
   * @param bodyText    the email body for clients not supporting HTML
   * @param bodyHTML    the email body (in HTML)
   * @param attachments the list of attachments (files)
   * @param toEmails    an array of email addresses to send the email to
   * @return the message id of the result
   */
  public String sendEmail(String fromEmail, String subject, String bodyText, String bodyHTML, List<File> attachments, String... toEmails)
      throws MessagingException, IOException {
    return sendEmail(fromEmail, subject, attachments.stream().map(a -> new EmailAttachment(a, a.getName())).collect(Collectors.toList()), bodyText, bodyHTML,
        toEmails);
  }

  /**
   * Sends an email with the specified parameters
   *
   * @param fromEmail   the email to sent it from (must be from a validated domain)
   * @param subject     the email subject
   * @param attachments the list of attachments
   * @param bodyText    the email body for clients not supporting HTML
   * @param bodyHTML    the email body (in HTML)
   * @param toEmails    an array of email addresses to send the email to
   * @return the message id of the result
   */
  public String sendEmail(String fromEmail, String subject, List<EmailAttachment> attachments, String bodyText, String bodyHTML, String... toEmails)
      throws MessagingException, IOException {
    return sendEmail(fromEmail, subject, attachments, bodyText, bodyHTML, new HashMap<>(), toEmails);
  }

  /**
   * Sends an email with the specified parameters
   *
   * @param fromEmail   the email to sent it from (must be from a validated domain)
   * @param subject     the email subject
   * @param attachments the list of attachments
   * @param bodyText    the email body for clients not supporting HTML
   * @param bodyHTML    the email body (in HTML)
   * @param headers     custom email headers, can be an empty map
   * @param toEmails    an array of email addresses to send the email to
   * @return the message id of the result
   */
  public String sendEmail(String fromEmail, String subject, List<EmailAttachment> attachments, String bodyText, String bodyHTML, Map<String, String> headers,
      String... toEmails) throws MessagingException, IOException {
    Session session = Session.getDefaultInstance(new Properties());

    // Create a new MimeMessage object.
    MimeMessage message = new MimeMessage(session);

    // Add subject, from and to lines.
    message.setSubject(subject, UTF_8);
    message.setFrom(new InternetAddress(fromEmail));

    InternetAddress[] addresses = new InternetAddress[toEmails.length];
    for (int i = 0; i < toEmails.length; i++) {
      addresses[i] = new InternetAddress(toEmails[i]);
    }

    for (Map.Entry<String, String> header : headers.entrySet()) {
      message.addHeader(header.getKey(), header.getValue());
    }

    message.setRecipients(Message.RecipientType.TO, addresses);

    // Create a multipart/alternative child container.
    MimeMultipart msgBody = new MimeMultipart("alternative");

    // Create a wrapper for the HTML and text parts.
    MimeBodyPart wrap = new MimeBodyPart();

    // Define the text part.
    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setContent(bodyText, "text/plain; charset=UTF-8");

    // Define the HTML part.
    MimeBodyPart htmlPart = new MimeBodyPart();
    htmlPart.setContent(bodyHTML, "text/html; charset=UTF-8");

    // Add the text and HTML parts to the child container.
    if (bodyText != null) {
      msgBody.addBodyPart(textPart);
    }
    msgBody.addBodyPart(htmlPart);

    // Add the child container to the wrapper object.
    wrap.setContent(msgBody);

    // Create a multipart/mixed parent container.
    MimeMultipart msg = new MimeMultipart("mixed");

    // Add the parent container to the message.
    message.setContent(msg);

    // Add the multipart/alternative part to the message.
    msg.addBodyPart(wrap);

    for (EmailAttachment attachment : attachments) {
      // Define the attachment
      MimeBodyPart att = new MimeBodyPart();
      DataSource fds = new FileDataSource(attachment.getFile());
      att.setDataHandler(new DataHandler(fds));
      att.setFileName(attachment.getName());

      // Add the attachment to the message.
      msg.addBodyPart(att);
    }

    // Send the email.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    message.writeTo(outputStream);
    RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

    SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage).withDestinations(toEmails);

    SendRawEmailResult sendRawEmailResult = client.sendRawEmail(rawEmailRequest);
    String emailList = Arrays.toString(toEmails);
    logger.info("{} Email sent to {}, result: {}", LOG_PREFIX, emailList, sendRawEmailResult);

    return sendRawEmailResult.getMessageId();
  }
}
