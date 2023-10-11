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
package com.ensolvers.fox.email;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sending emails
 *
 * @author Esteban Robles Luna
 */
public class EmailService {

  private static Logger logger = LoggerFactory.getLogger(EmailService.class);

  private String from;
  private String smtpUsername;
  private String smtpPassword;
  private String host;
  private int port = 587;

  public EmailService(String host, int port, String smptUsername, String smtpPassword, String fromMail) {
    this.from = fromMail;
    this.smtpUsername = smptUsername;
    this.smtpPassword = smtpPassword;
    this.host = host;
    this.port = port;
  }

  /**
   * Sends a transactional email from the configured account to mailTo
   *
   * @param mailTo  the to of the email
   * @param subject the subject
   * @param body    the body
   * @throws Exception
   */
  public void sendMailTo(String mailTo, String subject, String body) throws UnsupportedEncodingException, MessagingException {
    // Create a Properties object to contain connection configuration information.
    Properties props = System.getProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.port", this.port);
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.auth", "true");

    // Create a Session object to represent a mail session with the specified
    // properties.
    Session session = Session.getDefaultInstance(props);

    // Create a message with the specified information.
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(this.from, this.from));
    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
    msg.setSubject(subject);
    msg.setContent(body, "text/html");

    // Create a transport.
    Transport transport = session.getTransport();

    // Send the message.
    try {
      logger.info("Sending email...");

      // Connect to Amazon SES using the SMTP username and password you specified
      // above.
      transport.connect(this.host, this.smtpUsername, this.smtpPassword);

      // Send the email.
      transport.sendMessage(msg, msg.getAllRecipients());

      logger.info("Email sent!");
    } catch (Exception e) {
      logger.error("The email was not sent", e);
    } finally {
      // Close and terminate the connection.
      transport.close();
    }
  }
}
