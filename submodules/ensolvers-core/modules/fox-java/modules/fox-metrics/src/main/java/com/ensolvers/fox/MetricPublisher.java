package com.ensolvers.fox;

public interface MetricPublisher {
  void put(String dimensionName, String dimensionValue, String metricName, double value);
}
