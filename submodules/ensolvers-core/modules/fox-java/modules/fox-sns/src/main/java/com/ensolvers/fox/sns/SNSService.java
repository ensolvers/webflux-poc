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
package com.ensolvers.fox.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SNS service takes care of sending SMS notifications using AWS SNS Docs:
 * https://docs.aws.amazon.com/sns/latest/dg/sms_publish-to-phone.html
 *
 * @author Facundo Garbino
 */
public class SNSService {

  private static final Logger logger = LoggerFactory.getLogger(SNSService.class);
  private static final String LOG_PREFIX = "[AWS-SNS]";

  private final AmazonSNS client;

  public SNSService(AmazonSNS client) {
    this.client = client;
  }

  /**
   * Sends a SMS with the specified parameters
   *
   * @param senderId     the sender name that appears in the phone (3–11 alphanumeric characters, at least 1 letter and no
   *                     spaces)
   * @param phoneNumber  the phone number (in E.164 format, a maximum of 15 digits along with the prefix (+) and the
   *                     country code. For example, a US phone number in E.164 format appears as +1XXX5550100.
   * @param message      the message text (up to 140 bytes)
   * @param highPriority whether should be send with high priority (Transactional) or not (Promotional)
   * @param maxPrice     the maximum price to spend in the message (in USD)
   */
  public String sendSMSMessage(String senderId, String phoneNumber, String message, boolean highPriority, double maxPrice) {
    Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
    smsAttributes.put("AWS.SNS.SMS.SenderID", new MessageAttributeValue().withStringValue(senderId) // The sender ID
        // shown on the
        // device.
        .withDataType("String"));
    smsAttributes.put("AWS.SNS.SMS.MaxPrice", new MessageAttributeValue().withStringValue(Double.toString(maxPrice)).withDataType("Number"));
    smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue().withStringValue(highPriority ? "Transactional" : "Promotional") // Transactional
        // is
        // send
        // with
        // higher
        // priority
        .withDataType("String"));

    PublishResult result = this.client.publish(new PublishRequest().withMessage(message).withPhoneNumber(phoneNumber).withMessageAttributes(smsAttributes));

    logger.info("{} SMS sent to {}, result: {}", LOG_PREFIX, phoneNumber, result);
    return result.getMessageId();
  }

  /**
   * Retrieves a list of phone numbers that opted out SMS notifications
   * 
   * @author Facundo Errobidart
   */

  public List<String> listOptOut() {
    List<String> optedOuts = new ArrayList<>();
    ListPhoneNumbersOptedOutResult result = this.client.listPhoneNumbersOptedOut(new ListPhoneNumbersOptedOutRequest());
    optedOuts.addAll(result.getPhoneNumbers());

    while (result.getNextToken() != null) { // the result is paginated, each page has 100 numbers
      result = this.client.listPhoneNumbersOptedOut(new ListPhoneNumbersOptedOutRequest().withNextToken(result.getNextToken()));
      optedOuts.addAll(result.getPhoneNumbers());
    }
    return optedOuts;
  }
}
