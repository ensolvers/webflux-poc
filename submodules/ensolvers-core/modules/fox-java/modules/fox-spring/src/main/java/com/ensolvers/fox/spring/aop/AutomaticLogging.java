package com.ensolvers.fox.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It logs information for each invocation to target methods, allowing also to trace details of the execution (like
 * time spent). The following (optional) parameters can be provided:
 * <p>
 * - {@code logSuffix} specifies a suffix for every log entry written by the annotation - {@code
 * includeParameters} specifies if it must log the concrete parameters used in method call - {@code
 * logCollctionSizeOnly} when collections are passed as parameters and {@code includeParameteres} is true, it specifies
 * if all the inner parameters should be logged or only it must log the size - {@code timeElapsedLogging} specifies if
 * the time that passed between the invocation and the return of the function must be logged
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutomaticLogging {
  String logSuffix() default "";

  boolean includeParameters() default true;

  boolean logCollectionsSizeOnly() default true;

  boolean timeElapsedLogging() default false;
}
