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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The S3 service takes care of put,get and delete objects from S3
 *
 * @author Esteban Robles Luna
 */
public class S3Service implements S3ServiceInterface {

  private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
  private static final String LOG_PREFIX = "[AWS-S3-STORAGE]";

  private final AmazonS3Client s3Client;

  public S3Service(AmazonS3Client s3Client) {
    this.s3Client = s3Client;
  }

  /**
   * Generates a temporary URL to download a file from a private S3 bucket
   *
   * @param bucketName      the bucket
   * @param keyName         the path to the file
   * @param secondsToExpire the expiration time (in seconds)
   * @param fileName        the filename to be downloaded with
   * @return the temporary URL to download the object
   */
  public String generatePresignedUrl(String bucketName, String keyName, Long secondsToExpire, String fileName, boolean isInline) {
    logger.info("{}[START] Uploading a new object to S3 from a file, bucket=[{}], key=[{}]", LOG_PREFIX, bucketName, keyName);
    GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, keyName)
        .withExpiration(new Date(new Date().getTime() + secondsToExpire * 1000));
    // Generates a header with the name for the file to be downloaded with
    ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
    responseHeaders.setContentDisposition(isInline ? "inline" : "attachment" + "; filename =\"" + fileName + "\"");
    generatePresignedUrlRequest.setResponseHeaders(responseHeaders);

    URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    logger.info("{}[END] Uploading a new object to S3 from a file, bucket=[{}], key=[{}]", LOG_PREFIX, bucketName, keyName);
    return url.toString();
  }

  @Override
  public String generatePresignedUrl(String bucketName, String keyName, Long secondsToExpire, String fileName) {
    return generatePresignedUrl(bucketName, keyName, secondsToExpire, fileName, false);
  }

  public boolean doesObjectExist(String bucketName, String keyName) {
    return this.s3Client.doesObjectExist(bucketName, keyName);
  }

  /**
   * Sets the contents of file into the bucketName/keyName
   *
   * @param bucketName the bucket
   * @param keyName    the path to the file
   * @param file       the file to be uploaded
   */
  @Override
  public void put(String bucketName, String keyName, File file) {
    try {
      logger.info("{}[START] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], file=[{}]", LOG_PREFIX, bucketName, keyName,
          file.getAbsolutePath());

      PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, keyName, file);
      s3Client.putObject(putObjectRequest);

      logger.info("{}[END] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], file=[{}]", LOG_PREFIX, bucketName, keyName,
          file.getAbsolutePath());
    } catch (AmazonServiceException ase) {
      logger.error(LOG_PREFIX + " Caught an AmazonServiceException, which " + "means your request made it "
          + "to Amazon S3, but was rejected with an error response" + " for some reason.", ase);
    } catch (AmazonClientException ace) {
      logger.error(LOG_PREFIX + " Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to "
          + "communicate with S3, " + "such as not being able to access the network.", ace);
    }
  }

  /**
   * Sets the contents of the MultipartFile into the bucketName/keyName This overload of the put method simplifies the
   * image uploading process to S3
   *
   * @param bucketName the bucket
   * @param keyName    the path to the file
   */
  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead) {
    return put(bucketName, keyName, inputStream, size, isPublicRead, "");
  }

  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead, String contentType) {
    return put(bucketName, keyName, inputStream, size, isPublicRead, contentType, null);
  }

  /**
   * Sets the contents of the MultipartFile into the bucketName/keyName This overload of the put method simplifies the
   * image uploading process to S3
   *
   * @param bucketName  the bucket
   * @param keyName     the path to the file
   * @param contentType the file content type. Example: application/pdf
   */
  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead, String contentType,
      Map<String, String> userMetadata) {
    logger.info("{}[START] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], size={}", LOG_PREFIX, bucketName, keyName, size);

    var objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(size);

    if (userMetadata != null) {
      objectMetadata.setUserMetadata(userMetadata);
    }

    if (!contentType.equals("")) {
      objectMetadata.setContentType(contentType);
    }

    var request = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

    if (isPublicRead) {
      request.setCannedAcl(CannedAccessControlList.PublicRead);
    }

    s3Client.putObject(request);

    logger.info("{}[END] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], size={}", LOG_PREFIX, bucketName, keyName, size);

    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
  }

  /**
   * Sets the contents of the MultipartFile into the bucketName/keyName This overload of the put method simplifies the
   * image uploading process to S3
   *
   * @param bucketName  the bucket
   * @param keyName     the path to the file
   * @param contentType the file content type. Example: application/pdf
   */
  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, String contentType,
      CannedAccessControlList cannedAccessControlList) {
    logger.info("{}[START] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], size={}", LOG_PREFIX, bucketName, keyName, size);

    var objectMetadata = new ObjectMetadata();
    objectMetadata.setContentLength(size);

    if (!contentType.equals("")) {
      objectMetadata.setContentType(contentType);
    }

    var request = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

    if (cannedAccessControlList != null) {
      request.setCannedAcl(cannedAccessControlList);
    }

    s3Client.putObject(request);

    logger.info("{}[END] Uploading a new object to S3 from a file, bucket=[{}], key=[{}], size={}", LOG_PREFIX, bucketName, keyName, size);

    return String.format("https://%s.s3.amazonaws.com/%s", bucketName, keyName);
  }

  /**
   * Gets the contents of the file in bucketName/keyName
   *
   * @param bucketName the bucketName
   * @param keyName    the keyName
   * @return returns a local copy of the file in a temp directory
   */
  @Override
  public File get(String bucketName, String keyName) throws IOException {
    File tmpFile = File.createTempFile("fox", "s3");

    try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
      logger.info("{} [START] Getting data of object of S3", LOG_PREFIX);

      GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName);
      S3Object s3Object = s3Client.getObject(getObjectRequest);

      logger.info("{} [END] Getting data of object of S3", LOG_PREFIX);

      S3ObjectInputStream inputStream = s3Object.getObjectContent();

      IOUtils.copy(inputStream, fileOutputStream);
      IOUtils.closeQuietly(inputStream, null);
      IOUtils.closeQuietly(fileOutputStream, null);

      return tmpFile;
    } catch (AmazonServiceException ase) {
      logger.error(LOG_PREFIX + " Caught an AmazonServiceException, which " + "means your request made it "
          + "to Amazon S3, but was rejected with an error response" + " for some reason.", ase);
    } catch (AmazonClientException ace) {
      logger.error(LOG_PREFIX + " Caught an AmazonClientException, which " + "means the client encountered " + "an internal error while trying to "
          + "communicate with S3, " + "such as not being able to access the network.", ace);
    } catch (FileNotFoundException e) {
      logger.error(LOG_PREFIX + " not found", e);
    } catch (IOException e) {
      logger.error(LOG_PREFIX + " io error", e);
    }

    return null;
  }

  /**
   * Delete the object in bucketName/keyName
   *
   * @param bucketName the bucketName
   * @param keyName    the keyName
   */
  @Override
  public void delete(String bucketName, String keyName) {
    logger.info("{} [START] Deleting a object of S3", LOG_PREFIX);

    try {
      DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyName);
      s3Client.deleteObject(deleteObjectRequest);

      logger.info("{} [END] Deleting a object of S3", LOG_PREFIX);
    } catch (AmazonServiceException ase) {
      logger.error("{} Caught an AmazonServiceException. Error Message: {} HTTP Status Code: {} " + "AWS Error Code: {} Error Type: {} Request ID: {}",
          LOG_PREFIX, ase.getMessage(), ase.getStatusCode(), ase.getErrorCode(), ase.getErrorType(), ase.getRequestId());
    } catch (AmazonClientException ace) {
      logger.error("{} Caught an AmazonClientException. Error Message: {}", LOG_PREFIX, ace.getMessage());
    }
  }

  /**
   * List all the files in buckey under the folderKey, returning the list of file names
   *
   * @param bucket    the name of the buckey
   * @param folderKey the name of the container
   * @return the list of file names
   */
  @Override
  public List<String> list(String bucket, String folderKey) {
    ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucket).withPrefix(folderKey);

    ListObjectsV2Result result;
    List<String> keys = new ArrayList<>();
    do {
      result = this.s3Client.listObjectsV2(request);

      for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
        keys.add(objectSummary.getKey());
      }

      String token = result.getNextContinuationToken();
      request.setContinuationToken(token);

    } while (result.isTruncated());

    return keys;
  }
}
