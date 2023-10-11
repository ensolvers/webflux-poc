package com.ensolvers.fox.ses;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Testcontainers
class SESServiceTest {

  @Container
  public LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
      .withServices(LocalStackContainer.Service.SES);

  /**
   * Should send an email on two scenarios: - with Endpoint Configuration - with Region Configuration
   */
  @Test
  @Disabled("Status code 400")
  void testSES() {
    AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
        .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SES)).build();

    SESService service = new SESService(client);

    String from = "test@email.com";
    String[] to = { "receiver@test.com" };
    String body = "Test body";
    String subject = "Test subject";

    String sendId = service.sendEmail(from, subject, body, false, to);

    Assertions.assertFalse(sendId.isEmpty());

    client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(localstack.getDefaultCredentialsProvider()).build();

    service = new SESService(client);

    sendId = service.sendEmail(from, subject, body, false, to);

    Assertions.assertFalse(sendId.isEmpty());
  }

  /**
   * Should send an Email with a file attached on two scenarios: - with Endpoint Configuration - with Region Configuration
   *
   * @throws MessagingException Exception sending email
   * @throws IOException        Exception with attached file
   */
  @Test
  @Disabled("Status code 400 and file error")
  void testSESRawMessage() throws MessagingException, IOException {
    AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
        .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SES)).build();

    SESService service = new SESService(client);

    String from = "test@email.com";
    String[] to = { "receiver@test.com" };
    String body = "Test body";
    String htmlBody = "Test html body";
    String subject = "Test subject";
    File file = new File("../java/testUtils/dummyFile.txt");

    String sendId = service.sendEmail(from, subject, body, htmlBody, Collections.singletonList(file), to);
    Assertions.assertFalse(sendId.isEmpty());

    client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).withCredentials(localstack.getDefaultCredentialsProvider()).build();

    service = new SESService(client);

    sendId = service.sendEmail(from, subject, body, htmlBody, Collections.singletonList(file), to);
    Assertions.assertFalse(sendId.isEmpty());
  }
}
