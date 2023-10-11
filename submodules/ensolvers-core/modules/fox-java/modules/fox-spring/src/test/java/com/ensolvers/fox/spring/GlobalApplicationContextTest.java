package com.ensolvers.fox.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.ensolvers.fox.spring.lightweightcontainer.GlobalApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GlobalApplicationContextTest {

  @Autowired
  SampleComponent sampleComponent;

  @Test
  void testAutowiring() {
    assertNotNull(sampleComponent);
  }

  @Test
  void testBeanLookUp() {
    SampleComponent sc = (SampleComponent) GlobalApplicationContext.getBean("sampleComponent");
    assertNotNull(sc);
    assertEquals(sampleComponent, sc);
    assertEquals("Hey there", sc.helloWorld());
  }
}
