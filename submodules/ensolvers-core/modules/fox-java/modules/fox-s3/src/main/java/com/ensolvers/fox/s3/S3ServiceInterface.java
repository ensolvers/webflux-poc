package com.ensolvers.fox.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public interface S3ServiceInterface {
  public String generatePresignedUrl(String bucketName, String keyName, Long secondsToExpire, String fileName) throws IOException;

  public void put(String bucketName, String keyName, File file);

  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead) throws IOException;

  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead, String contentType) throws IOException;

  public String put(String bucketName, String keyName, InputStream inputStream, long size, String contentType, CannedAccessControlList cannedAccessControlList)
      throws IOException;

  public File get(String bucketName, String keyName) throws IOException;

  public void delete(String bucketName, String keyName) throws IOException;

  public List<String> list(String bucket, String folderKey);
}
