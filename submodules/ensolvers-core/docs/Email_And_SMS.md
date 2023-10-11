# Email and SMS sending

Core supports email and SMS sending functionality through Amazon Simple Email Service (SES) and Amazon Simple Notification Service (SNS) respectively. 

## Emailing

Email sending is implemented by the class [`CoreEmailService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/email/CoreEmailService.java) which provides several methods for that
purpose. Check class docs for more detail.

To check how this class must be configured, have a look to [`EmailConfiguration`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EmailConfiguration.java) class, which contains
the core variables involved.

### Legacy implementation

The legacy implementation for email sending is implemented by `SESEmailService` and `LoggingEmailService`, check  
[`EmailConfiguration`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/configuration/EmailConfiguration.java)
to check how these classes are instantiated

## SMS sending

On the other hand, SMS sending is implemented by the class [`SNSMessageService`](../modules/ensolvers-core-common/src/main/java/com/ensolvers/core/common/services/SNSMessageService.java) which provides 2 methods for this:
- `postMessage`
- `sendMessage`

Details on all these methods are documented directly in the classes.