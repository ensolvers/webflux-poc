package com.ensolvers.fox.quality.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a set of historic values of a particular metric
 *
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
public class SonarQubeMetricHistory {

  String metric;
  List<SonarQubeMetricMeasure> history;

  protected SonarQubeMetricHistory() {
  }

  public SonarQubeMetricHistory(String metric) {
    this.metric = metric;
    this.history = new ArrayList<>();
  }

  public SonarQubeMetricHistory(String metric, List<SonarQubeMetricMeasure> history) {
    this.metric = metric;
    this.history = history;
  }

  public String getMetric() {
    return metric;
  }

  public List<SonarQubeMetricMeasure> getHistory() {
    return history;
  }

  public void addMeasure(SonarQubeMetricMeasure value) {
    this.history.add(value);
  }
}
