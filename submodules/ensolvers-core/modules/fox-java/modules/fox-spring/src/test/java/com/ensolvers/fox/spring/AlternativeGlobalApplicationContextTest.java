package com.ensolvers.fox.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import com.ensolvers.fox.spring.lightweightcontainer.GlobalApplicationContext;
import org.junit.jupiter.api.Test;

public class AlternativeGlobalApplicationContextTest {

  @Test
  public void testResourceSample() {
    GlobalApplicationContext.build("classpath*:/com/ensolvers/fox/spring/sameAutowiring-context.xml");
    SampleComponent sc = (SampleComponent) GlobalApplicationContext.getBean("sampleComponent");
    assertNotNull(sc);
    assertEquals("Hey there", sc.helloWorld());

  }

  @Test
  public void testPointcutResourceSample() {
    GlobalApplicationContext.build("classpath*:/com/ensolvers/fox/spring/pointcutAutowiring-context.xml");

    try {
      GlobalApplicationContext.getBean("sampleComponent");
      fail();
    } catch (Exception e) {
    }

    assertNotNull(GlobalApplicationContext.getBean("automaticLoggingImpl"));
  }

}