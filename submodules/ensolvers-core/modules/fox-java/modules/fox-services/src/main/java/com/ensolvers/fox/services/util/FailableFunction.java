package com.ensolvers.fox.services.util;

@FunctionalInterface
public interface FailableFunction<T, R, E extends Exception> {

  R apply(T t) throws E;

}
