package com.ensolvers.fox.services.logging;

import ch.qos.logback.classic.Level;
import com.newrelic.api.agent.NewRelic;
import java.util.function.Supplier;

/**
 * This service class logger avoids the need to structurally declare a Logger instance in every client class definition.
 * <p>
 * Logs in the right category when logging within a polymorphic hierarchy.
 * <p>
 * Notifies NewRelic when present
 * <p>
 * <p>
 * Information to get to the right category is obtained from the instance class.
 * <p>
 * Within a static context ClassName.class() should be used.
 * <p>
 * <p>
 * <p>
 * Accessing info and debug categories is efficient and logging attempt is avoided when inactive
 * <p>
 * <p>
 * <p>
 * <code>debug(...)</code>, <code>info(...)</code>, <code>warn(...)</code> and <code>error(...)</code> methods families
 * provide dynamic access to according level categories with or without exception as parameter
 * <p>
 * <p>
 * <p>
 * <code>is[debug|info]Enabled</code> allows testing on level activation
 * <p>
 * <p>
 * <p>
 * <code>set[debug|info|warn|error]Level( category )</code> allows to set log level for a <code>category</code>
 * <p>
 *
 * @deprecated Please don't use this class since it demostrated in practice that is more error prone and omits the full
 * stack traces in several cases. Use {@link CoreLogger} instead.
 */
@Deprecated()
public class Logger {
  enum ExternalStatus {
    Unknown, Present, Absent
  }

  private static ExternalStatus newRelicStatus = ExternalStatus.Unknown;

  private Logger() {
  }

  private static String customizeMsg(Object msg) {
    return msg.toString();
  }

  public static void info(Object category, Supplier<String> fn) {
    info(category.getClass(), fn);
  }

  public static void info(Class<?> category, Supplier<String> fn) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isInfoEnabled()) {
      c.info(fn.get());
    }
  }

  public static void info(Object category, String msg) {
    info(category.getClass(), msg);
  }

  public static void info(Class category, String msg) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isInfoEnabled()) {
      c.info(customizeMsg(msg));
    }
  }

  public static void info(Object category, Object msg, Throwable e) {
    info(category.getClass(), msg, e);
  }

  public static void info(Class category, Object msg, Throwable e) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isInfoEnabled()) {
      c.info(customizeMsg(msg), e);
    }
  }

  public static void info(Object category, Object msg) {
    info(category.getClass(), msg);
  }

  public static void info(Class category, Object msg) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isInfoEnabled()) {
      c.info(customizeMsg(msg));
    }
  }

  public static void debug(Object category, Supplier<String> fn) {
    debug(category.getClass(), fn);
  }

  public static void debug(Class<?> category, Supplier<String> fn) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isDebugEnabled()) {
      c.debug(customizeMsg(fn.get()));
    }
  }

  public static void debug(Object category, Object msg) {
    debug(category.getClass(), msg);
  }

  public static void debug(Class category, Object msg) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isDebugEnabled()) {
      c.debug(customizeMsg(msg));
    }
  }

  public static void debug(Object category, Object msg, Throwable e) {
    debug(category.getClass(), msg, e);
  }

  public static void debug(Class category, Object msg, Throwable e) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isDebugEnabled()) {
      c.debug(customizeMsg(msg), e);
    }
  }

  public static void debug(Object category, String msg) {
    debug(category.getClass(), msg);
  }

  public static void debug(Class category, String msg) {
    org.slf4j.Logger c = getCategory(category);
    if (c.isDebugEnabled()) {
      c.debug(customizeMsg(msg));
    }
  }

  public static void error(Object category, Object msg, Throwable e) {
    error(category.getClass(), msg, e);
  }

  public static void error(Class category, Object msg, Throwable e) {
    String cMsg = customizeMsg(msg);
    notifyExternals(cMsg);
    getCategory(category).error(cMsg, e);
  }

  public static void error(Object category, Object msg) {
    error(category.getClass(), msg);
  }

  public static void error(Class category, Object msg) {
    String cMsg = customizeMsg(msg);
    notifyExternals(cMsg);
    getCategory(category).error(cMsg);
  }

  public static void error(Object category, String msg) {
    error(category.getClass(), msg);
  }

  public static void error(Class category, String msg) {
    notifyExternals(msg);
    getCategory(category).error(msg);
  }

  public static void warn(Object category, Object msg) {
    warn(category.getClass(), msg);
  }

  public static void warn(Class category, Object msg) {
    getCategory(category).warn(customizeMsg(msg));
  }

  public static void warn(Object category, Object msg, Throwable e) {
    warn(category.getClass(), msg, e);
  }

  public static void warn(Class category, Object msg, Throwable e) {
    getCategory(category).warn(customizeMsg(msg), e);
  }

  public static boolean isInfoEnabled(Object category) {
    return getCategory(category.getClass()).isInfoEnabled();
  }

  public static boolean isInfoEnabled(Class category) {
    return getCategory(category).isInfoEnabled();
  }

  public static boolean isDebugEnabled(Object category) {
    return getCategory(category.getClass()).isDebugEnabled();
  }

  public static boolean isDebugEnabled(Class category) {
    return getCategory(category).isDebugEnabled();
  }

  public static void initInfo(Object o, String methodName) {
    initInfo(o.getClass(), methodName);
  }

  public static void initInfo(Class o, String methodName) {
    getCategory(o).info("{} [Init]", methodName);
  }

  public static void endInfo(Object o, String methodName) {
    endInfo(o.getClass(), methodName);
  }

  public static void endInfo(Class o, String methodName) {
    getCategory(o).info("{} [End]", methodName);
  }

  public static org.slf4j.Logger getCategory(Class<?> clazz) {
    return org.slf4j.LoggerFactory.getLogger(clazz);
  }

  public static org.slf4j.Logger getCategory(String clazz) {
    return org.slf4j.LoggerFactory.getLogger(clazz);
  }

  public static void setDebugLevel(Class<?> clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.DEBUG);
  }

  public static void setDebugLevel(String clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.DEBUG);
  }

  public static void setInfoLevel(Class<?> clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.INFO);
  }

  public static void setInfoLevel(String clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.INFO);
  }

  public static void setWarnLevel(Class<?> clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.WARN);
  }

  public static void setWarnLevel(String clazz) {
    ((ch.qos.logback.classic.Logger) getCategory(clazz)).setLevel(Level.WARN);
  }

  private static void notifyExternals(String cMsg) {
    if (newRelicStatus == ExternalStatus.Unknown) {
      try {
        Class.forName("com.newrelic.api.agent.NewRelic");
        newRelicStatus = ExternalStatus.Present;
      } catch (ClassNotFoundException e) {
        newRelicStatus = ExternalStatus.Absent;
        getCategory(Logger.class).info("NewRelic not available");
        return;
      }
    }
    if (newRelicStatus == ExternalStatus.Present)
      NewRelic.noticeError(cMsg);
  }
}