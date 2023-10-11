package com.ensolvers.fox.spring.lightweightcontainer;

import com.ensolvers.fox.services.logging.CoreLogger;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * GlobalApplicationContext is a Spring companion.
 * <p>
 * Has two main features: look up and build.
 * <p>
 * <p>
 * <p>
 * * Look up allows to get from Spring context any registered bean
 * <p>
 * Example: <code> MyComponent component = (MyComponent)GlobalApplicationContext.getBean( "MyComponent" );</code>
 * <p>
 * <p>
 * <p>
 * * Construction allows building on the fly new context by offering legacy configuration xml files.
 * <p>
 * <p>
 * See AlternativeGlobalApplicationContextTest tests.
 * <p>
 * <p>
 * <p>
 * Static accessors:
 * <p>
 * <p>
 * <code>getBean</code> looks up a Spring bean.
 * <p>
 * <p>
 * <code>setGlobalContext</code> and <code>getInstance</code> are user static accessors to the actual
 * ApplicationContext.
 * <p>
 * <p>
 * <p>
 * Context Building for Testing Scenery Set Up:
 * <p>
 * <p>
 * <code>build</code> builds an ApplicationContext from a xml Context file.
 * <p>
 * <p>
 * See AlternativeGlobalApplicationContextTest tests.
 * <p>
 * <p>
 * <p>
 * Accessors:
 * <p>
 * <p>
 * <code>setApplicationContext</code> is the mandatory ApplicationContextAware interface ApplicationContext instance
 * accessor.
 * <p>
 * <p>
 */
@Component
public class GlobalApplicationContext implements ApplicationContextAware {
  private static ApplicationContext context;

  private static Logger logger = CoreLogger.getLogger(GlobalApplicationContext.class);

  public static Object getBean(String name) {
    if (context == null) {
      logger.error("No Spring Context available");
      throw new ApplicationContextException("No Spring Context available");
    }
    return getInstance().getBean(name);
  }

  public static void setGlobalContext(ApplicationContext globalContext) {
    context = globalContext;
  }

  public static ApplicationContext getInstance() {
    return context;
  }

  public static ApplicationContext build(String config) {
    setGlobalContext(new ClassPathXmlApplicationContext(config));
    return getInstance();
  }

  // ApplicationContextAware interface
  public void setApplicationContext(ApplicationContext ac) throws BeansException {
    setGlobalContext(ac);
  }
}