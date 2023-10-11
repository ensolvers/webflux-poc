package com.ensolvers.fox.s3;

import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Local implementation of the S3Service interface
 */
public class LocalS3Service implements S3ServiceInterface {

  private String storagePath;

  public LocalS3Service() {
    try {
      this.storagePath = String.valueOf(Files.createTempDirectory("LocalS3Service").toAbsolutePath());
    } catch (IOException e) {
      this.storagePath = "./tmp/LocalS3Service";
    }
  }

  public String generatePresignedUrl(String bucketName, String keyName, Long secondsToExpire, String fileName) throws IOException {
    return this.get(bucketName, keyName).getAbsolutePath();
  };

  @Override
  public void put(String bucketName, String keyName, File file) {
    try {
      Path targetDirectory = Paths.get(storagePath, bucketName);
      Files.createDirectories(targetDirectory);
      Files.copy(file.toPath(), targetDirectory.resolve(keyName), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ignored) {
    }
  }

  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead) throws IOException {
    return put(bucketName, keyName, inputStream);
  }

  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, boolean isPublicRead, String contentType) throws IOException {
    return put(bucketName, keyName, inputStream);
  }

  @Override
  public String put(String bucketName, String keyName, InputStream inputStream, long size, String contentType, CannedAccessControlList cannedAccessControlList)
      throws IOException {
    return put(bucketName, keyName, inputStream);
  }

  private String put(String bucketName, String keyName, InputStream inputStream) throws IOException {
    Path targetDirectory = Paths.get(storagePath, bucketName);
    Files.createDirectories(targetDirectory);
    Files.copy(inputStream, targetDirectory.resolve(keyName), StandardCopyOption.REPLACE_EXISTING);
    return keyName;
  }

  @Override
  public File get(String bucketName, String keyName) throws IOException {
    Path filePath = Paths.get(storagePath, bucketName, keyName);
    if (Files.exists(filePath)) {
      return filePath.toFile();
    }
    throw new IOException("File not found: " + keyName);
  }

  @Override
  public void delete(String bucketName, String keyName) throws IOException {
    Path filePath = Paths.get(storagePath, bucketName, keyName);
    Files.deleteIfExists(filePath);
  }

  @Override
  public List<String> list(String bucket, String folderKey) {
    Path folderPath = Paths.get(storagePath, bucket, folderKey);
    if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
      try (Stream<Path> paths = Files.list(folderPath)) {
        return paths.map((p) -> p.getFileName().toString()).collect(Collectors.toList());
      } catch (Exception ignored) {
      }
    }

    return List.of();
  }
}
