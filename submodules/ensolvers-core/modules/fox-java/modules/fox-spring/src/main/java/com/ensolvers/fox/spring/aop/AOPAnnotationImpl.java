package com.ensolvers.fox.spring.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;

public class AOPAnnotationImpl {

  protected <T extends Annotation> T getAnnotationFromContext(JoinPoint joinPoint, Class<T> annotationClass) throws Exception {
    // first we try to get the annotation at the method level
    T annotation = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(annotationClass);

    // if annotation not found at the method level, then we try at a class level
    if (annotation == null) {
      annotation = joinPoint.getTarget().getClass().getAnnotation(annotationClass);
    }

    if (annotation == null) {
      throw new Exception("Annotation " + annotationClass + " not found");
    }

    return annotation;
  }
}
