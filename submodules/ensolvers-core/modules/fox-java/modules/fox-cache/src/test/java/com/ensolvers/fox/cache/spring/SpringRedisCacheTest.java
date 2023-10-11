package com.ensolvers.fox.cache.spring;

import com.ensolvers.fox.cache.spring.context.config.MemcachedCacheConfig;
import com.ensolvers.fox.cache.spring.context.config.RedisCacheConfig;
import com.ensolvers.fox.cache.spring.context.objects.SampleComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ContextConfiguration(classes = { RedisCacheConfig.class, SampleComponent.class })
@Testcontainers
class SpringRedisCacheTest {
  @Autowired
  SampleComponent sampleComponent;

  @Container
  public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:6.2.5")).withExposedPorts(6379);

  @DynamicPropertySource
  public static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("cache.redis.port", () -> redisContainer.getFirstMappedPort());
  }

  @Test
  void testGet() {
    CacheTester.testGet(sampleComponent);
  }

  @Test
  void testGetComplexObjects() {
    CacheTester.testGetComplexObjects(sampleComponent);
  }

  @Test
  void testBulkGetComplexObjects() {
    CacheTester.testBulkGetComplexObjects(sampleComponent);
  }

  @Test
  void testNullValues() {
    CacheTester.testNullValuesInBulkGet(sampleComponent);
  }

  @Test
  void testPut() {
    CacheTester.testPut(sampleComponent);
  }

  @Test
  void testInvalidate() {
    CacheTester.testInvalidate(sampleComponent);
  }
}
