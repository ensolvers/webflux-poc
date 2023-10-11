package com.ensolvers.fox.spring.aop;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/** AutomaticLogging default implementation */
@Aspect
@Configuration
@Component
public class AutomaticLoggingImpl {

  public static boolean switchType(Object o, Consumer... a) {
    for (Consumer consumer : a)
      if (o instanceof Collection || o instanceof Map) {
        consumer.accept(o);
        return true;
      }
    return false;
  }

  public static <T> Consumer<T> caze(Class<T> cls, Consumer<T> c) {
    return obj -> Optional.of(obj).filter(cls::isInstance).map(cls::cast).ifPresent(c);
  }

  @Around("@annotation(com.ensolvers.fox.spring.aop.AutomaticLogging)")
  public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();

    /* The names and values of the target parameters are captured, as well as the values in the annotation. */
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    Logger logger = LoggerFactory.getLogger(AutomaticLogging.class);
    AutomaticLogging annotation = methodSignature.getMethod().getAnnotation(AutomaticLogging.class);
    String className = joinPoint.getTarget().getClass().getName();
    String[] classNameContent = className.split("\\.");
    className = classNameContent[classNameContent.length - 1];

    String suffix = (!annotation.logSuffix().isEmpty() ? "[" + annotation.logSuffix() + "] " : "");
    String call = annotation.timeElapsedLogging() ? "[Call] " : "";
    String logMsg = String.format("%s[%s] [%s] %s", suffix, className, methodSignature.getName(), call);
    AtomicReference<String> logContent = new AtomicReference<>(logMsg);
    String[] parameterNames = methodSignature.getParameterNames();
    Object[] parameterValues = joinPoint.getArgs();

    /*  Iterate between the parameters if the option has not been disabled in the annotation and add them to the String */
    if (annotation.includeParameters()) {
      addParametersToLog(annotation, logContent, parameterNames, parameterValues);
    }

    logger.info(logContent.get());

    /*   We proceed with the execution of the target */
    Object proceed = null;
    try {
      proceed = joinPoint.proceed();

      /*  When it is finished, it is calculated how long it took and the message is updated to be logged again if TimeElapsedLogging is activated */
      if (annotation.timeElapsedLogging()) {
        long executionTime = System.currentTimeMillis() - start;
        logContent.set(logContent.get().replace("[Call]", "[Finish]"));
        logContent.set(logContent.get() + "elapsed ms: " + executionTime + " (" + TimeUnit.MILLISECONDS.toSeconds(executionTime) + " sec)");
        logger.info(logContent.get());
      }
    } catch (Exception error) {
      String errorMsg = String.format("%s[%s] [%s] Error: %s", suffix, className, methodSignature.getName(), error);
      logger.error(errorMsg);
      throw error;
    }

    return proceed;
  }

  private void addParametersToLog(AutomaticLogging annotation, AtomicReference<String> logContent, String[] parameterNames, Object[] parameterValues) {
    if (annotation.logCollectionsSizeOnly()) {
      /* If the parameters are Collections and the logCollectionsSizeOnly() parameter of the Annotation is set to True, only the size will be reported and not the content. */
      for (int i = 0; i < parameterNames.length; i++) {
        logContent.set(logContent.get() + parameterNames[i]);
        boolean collectionParam = switchType(parameterValues[i], caze(Collection.class, c -> logContent.set(logContent.get() + " size : " + c.size() + " ")),
            caze(Map.class, a -> logContent.set(logContent.get() + " size : " + a.size() + " ")));
        if (!collectionParam) {
          logContent.set(logContent.get() + ": " + (parameterValues[i] != null ? parameterValues[i].toString() : "null") + " ");
        }
      }
    } else {
      for (int i = 0; i < parameterNames.length; i++) {
        logContent.set(logContent.get() + parameterNames[i] + ": " + parameterValues[i].toString() + " ");
      }
    }
  }
}
