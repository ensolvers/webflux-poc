package com.ensolvers.fox.services.logging;

import com.newrelic.api.agent.NewRelic;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.helpers.MessageFormatter;

/**
 * We are going to generalize all our caching functionality in {@link CoreLogger class}
 */
@Deprecated(since = "2023-07-14", forRemoval = true)
public class NewRelicLogger implements Logger {

  private Logger logger;
  private Date timer;

  public NewRelicLogger(Logger logger) {
    super();
    this.logger = logger;
  }

  public static NewRelicLogger getLogger(Class<?> aClass) {
    return new NewRelicLogger(LoggerFactory.getLogger(aClass));
  }

  @Override
  public String getName() {
    return this.logger.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return this.logger.isTraceEnabled();
  }

  @Override
  public void trace(String msg) {
    this.logger.trace(msg);
  }

  @Override
  public void trace(String format, Object arg) {
    this.logger.trace(format, arg);
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    this.logger.trace(format, arg1, arg2);
  }

  @Override
  public void trace(String format, Object... arguments) {
    this.logger.trace(format, arguments);
  }

  @Override
  public void trace(String msg, Throwable t) {
    this.logger.trace(msg, t);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return this.logger.isTraceEnabled();
  }

  @Override
  public void info(String msg) {
    this.logger.info(msg);
  }

  @Override
  public void info(String format, Object arg) {
    this.logger.info(format, arg);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    this.logger.info(format, arg1, arg2);
  }

  @Override
  public void info(String format, Object... arguments) {
    this.logger.info(format, arguments);
  }

  @Override
  public void info(String msg, Throwable t) {
    this.logger.info(msg, t);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return this.logger.isInfoEnabled();
  }

  @Override
  public void info(Marker marker, String msg) {
    this.logger.info(marker, msg);
  }

  @Override
  public void info(Marker marker, String format, Object arg) {
    this.logger.info(marker, format, arg);
  }

  @Override
  public void info(Marker marker, String format, Object arg1, Object arg2) {
    this.logger.info(marker, format, arg1, arg2);
  }

  @Override
  public void info(Marker marker, String format, Object... argArray) {
    this.logger.info(marker, format, argArray);
  }

  @Override
  public void info(Marker marker, String msg, Throwable t) {
    this.logger.info(marker, msg, t);
  }

  @Override
  public void trace(Marker marker, String msg) {
    this.logger.trace(marker, msg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg) {
    this.logger.trace(marker, format, arg);
  }

  @Override
  public void trace(Marker marker, String format, Object arg1, Object arg2) {
    this.logger.trace(marker, format, arg1, arg2);
  }

  @Override
  public void trace(Marker marker, String format, Object... argArray) {
    this.logger.trace(marker, format, argArray);
  }

  @Override
  public void trace(Marker marker, String msg, Throwable t) {
    this.logger.trace(marker, msg, t);
  }

  @Override
  public void debug(String msg) {
    this.logger.debug(msg);
  }

  @Override
  public void debug(String format, Object arg) {
    this.logger.debug(format, arg);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    this.logger.debug(format, arg1, arg2);
  }

  @Override
  public void debug(String format, Object... arguments) {
    this.logger.debug(format, arguments);
  }

  @Override
  public void debug(String msg, Throwable t) {
    this.logger.debug(msg, t);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return this.logger.isDebugEnabled();
  }

  @Override
  public void debug(Marker marker, String msg) {
    this.logger.debug(marker, msg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg) {
    this.logger.debug(marker, format, arg);
  }

  @Override
  public void debug(Marker marker, String format, Object arg1, Object arg2) {
    this.logger.debug(marker, format, arg1, arg2);
  }

  @Override
  public void debug(Marker marker, String format, Object... argArray) {
    this.logger.debug(marker, format, argArray);
  }

  @Override
  public void debug(Marker marker, String msg, Throwable t) {
    this.logger.debug(marker, msg, t);
  }

  @Override
  public void warn(String msg) {
    this.logger.warn(msg);
  }

  @Override
  public void warn(String format, Object arg) {
    this.logger.warn(format, arg);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    this.logger.warn(format, arg1, arg2);
  }

  @Override
  public void warn(String format, Object... arguments) {
    this.logger.warn(format, arguments);
  }

  @Override
  public void warn(String msg, Throwable t) {
    this.logger.warn(msg, t);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return this.logger.isWarnEnabled();
  }

  @Override
  public void warn(Marker marker, String msg) {
    this.logger.warn(marker, msg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg) {
    this.logger.warn(marker, format, arg);
  }

  @Override
  public void warn(Marker marker, String format, Object arg1, Object arg2) {
    this.logger.warn(marker, format, arg1, arg2);
  }

  @Override
  public void warn(Marker marker, String format, Object... argArray) {
    this.logger.warn(marker, format, argArray);
  }

  @Override
  public void warn(Marker marker, String msg, Throwable t) {
    this.logger.warn(marker, msg, t);
  }

  @Override
  public void error(String msg) {
    NewRelic.noticeError(msg);
    this.logger.error(msg);
  }

  @Override
  public void error(String format, Object arg) {
    NewRelic.noticeError(MessageFormatter.format(format, arg).getMessage());
    this.logger.error(format, arg);
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    NewRelic.noticeError(MessageFormatter.format(format, arg1, arg2).getMessage());
    this.logger.error(format, arg1, arg2);
  }

  @Override
  public void error(String format, Object... arguments) {
    NewRelic.noticeError(MessageFormatter.arrayFormat(format, arguments).getMessage());
    this.logger.error(format, arguments);
  }

  @Override
  public void error(String msg, Throwable t) {
    NewRelic.noticeError(t);
    this.logger.error(msg, t);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return this.logger.isErrorEnabled();
  }

  @Override
  public void error(Marker marker, String msg) {
    NewRelic.noticeError(msg);
    this.logger.error(marker, msg);
  }

  @Override
  public void error(Marker marker, String format, Object arg) {
    NewRelic.noticeError(MessageFormatter.format(format, arg).getMessage());
    this.logger.error(marker, format, arg);
  }

  @Override
  public void error(Marker marker, String format, Object arg1, Object arg2) {
    NewRelic.noticeError(MessageFormatter.format(format, arg1, arg2).getMessage());
    this.logger.error(marker, format, arg1, arg2);
  }

  @Override
  public void error(Marker marker, String format, Object... argArray) {
    NewRelic.noticeError(MessageFormatter.arrayFormat(format, argArray).getMessage());
    this.logger.error(marker, format, argArray);
  }

  @Override
  public void error(Marker marker, String msg, Throwable t) {
    NewRelic.noticeError(t);
    this.logger.error(marker, msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return this.logger.isDebugEnabled();
  }

  @Override
  public boolean isInfoEnabled() {
    return this.logger.isInfoEnabled();
  }

  @Override
  public boolean isWarnEnabled() {
    return this.logger.isWarnEnabled();
  }

  @Override
  public boolean isErrorEnabled() {
    return this.logger.isErrorEnabled();
  }

  public void initTimer() {
    this.timer = new Date();
  }

  public void stopTimer() {
    this.timer = null;
  }

  public void stopTimer(String msg) {
    timeElapsed(msg);
    stopTimer();
  }

  public Date timeElapsed(String msg) {
    Date now = new Date();
    this.logger.info(msg);
    return now;
  }

  public Date timeElapsed(String msg, Date since) {
    Date now = new Date();
    long elapsed = now.getTime() - since.getTime();
    this.logger.info("{} elapsed ms: {} ( {} sec)", msg, elapsed, TimeUnit.MILLISECONDS.toSeconds(elapsed));
    return now;
  }

  public void finfo(String format, Object... args) {
    this.info(format, args);
  }

  public void ftrace(String format, Object... args) {
    this.trace(format, args);
  }

  public void fdebug(String format, Object... args) {
    this.debug(format, args);
  }

  public void ferror(String format, Throwable t, Object... args) {
    this.error(MessageFormatter.arrayFormat(format, args).getMessage(), t);
  }

  public void ferror(String format, Object... args) {
    this.error(format, args);
  }
}
