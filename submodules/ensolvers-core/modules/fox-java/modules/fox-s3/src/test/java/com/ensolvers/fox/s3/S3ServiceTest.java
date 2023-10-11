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
package com.ensolvers.fox.s3;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import java.io.*;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * A Test case for {@link S3Service}
 *
 * @author Esteban Robles Luna
 */
@Testcontainers
class S3ServiceTest {

  @Container
  public LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:0.11.3"))
      .withServices(LocalStackContainer.Service.S3);

  @Test
  void testS3() throws Exception {
    String bucket = "foxtest";
    String testData = "this is a sample test data";
    String key = "t1";
    String folderName = "f1";

    AmazonS3Client client = (AmazonS3Client) AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.S3))
        .withCredentials(localstack.getDefaultCredentialsProvider()).build();

    S3Service service = new S3Service(client);
    // prepares the bucket
    client.createBucket(bucket);

    // read non existent file
    File file = service.get(bucket, key);
    assertNull(file);

    // no fail
    service.delete(bucket, key);

    // write file in1 root context
    File f = File.createTempFile("ensolversfox", ".txt");
    FileUtils.writeStringToFile(f, testData, "UTF8");
    service.put(bucket, key, f);
    f.delete();

    // read existant file
    file = service.get(bucket, key);
    assertNotNull(file);
    String contents = FileUtils.readFileToString(file, "UTF8");
    assertEquals(testData, contents);

    // no fail
    service.delete(bucket, key);

    file = service.get(bucket, key);
    assertNull(file);

    List<String> keys = service.list(bucket, folderName);
    assertTrue(keys.isEmpty());

    // write file in folder
    f = File.createTempFile("ensolversfox", ".txt");
    FileUtils.writeStringToFile(f, testData, "UTF8");
    service.put(bucket, folderName + "/" + key, f);
    f.delete();

    // now folder shouldn't be empty
    keys = service.list(bucket, folderName);
    assertFalse(keys.isEmpty());
  }
}
