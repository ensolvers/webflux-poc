# SMS

Core supports SMS functionality through `Amazon Simple Notification Service` (SNS).
The class `SNSMessageService` implements the interface `SMSService` than has 2 methods:

- **postSMS:** Saves an SMS into the DB and tries to send it. If the message was sent, updates the aws message id.

```
   @param senderId     the sender name that appears in the phone (3–11 alphanumeric characters, at least 1 letter and no
                       spaces)
   @param phoneNumber  the phone number (in E.164 format, a maximum of 15 digits along with the prefix (+) and the
                       country code. For example, a US phone number in E.164 format appears as +1XXX5550100.
   @param message      the message text (up to 140 bytes)
   @param highPriority whether should be sent with high priority (Transactional) or not (Promotional)
   @param maxPrice     the maximum price to spend in the message (in USD)
   @return SMS external id
```

```
   @param senderId     the sender name that appears in the phone (3–11 alphanumeric characters, at least 1 letter and no
                       spaces)
   @param phoneNumber  the phone number (in E.164 format, a maximum of 15 digits along with the prefix (+) and the
                       country code. For example, a US phone number in E.164 format appears as +1XXX5550100.
   @param message      the message text (up to 140 bytes)
   @return SMS external id

```

- **sendSMS:** Sends an SMS with the specified parameters.

```
   @param senderId     the sender name that appears in the phone (3–11 alphanumeric characters, at least 1 letter and no
                       spaces)
   @param phoneNumber  the phone number (in E.164 format, a maximum of 15 digits along with the prefix (+) and the
                       country code. For example, a US phone number in E.164 format appears as +1XXX5550100.
   @param message      the message text (up to 140 bytes)
   @param highPriority whether should be sent with high priority (Transactional) or not (Promotional)
   @param maxPrice     the maximum price to spend in the message (in USD)
   @return provider message id
```
