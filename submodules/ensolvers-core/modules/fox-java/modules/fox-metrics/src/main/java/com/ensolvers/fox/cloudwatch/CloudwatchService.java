/* Copyright (c) 2021 Ensolvers
 * All Rights Reserved
 *
 * The contents of this file is dual-licensed under 2 alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to the project.
 *
 * You may obtain a copy of the LGPL License at: http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at: http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */
package com.ensolvers.fox.cloudwatch;

import com.ensolvers.fox.MetricPublisher;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

/**
 * Service which simplifies metric pushing to AWS Cloudwatch
 *
 * @author José Matías Rivero
 */
public class CloudwatchService implements MetricPublisher {

  private final String namespace;
  private final CloudWatchClient client;

  public CloudwatchService(String accessKeyId, String secretAccessKeyId, Region region, String namespace) {
    this.namespace = namespace;

    this.client = this.createClient(accessKeyId, secretAccessKeyId, region);
  }

  private CloudWatchClient createClient(String accessKeyId, String secretAccessKeyId, Region region) {

    return CloudWatchClient.builder().region(region).credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKeyId)).build();
  }

  /**
   * Publishes a new value for a specific metric using the default unit (StandardUnit.NONE) Note: the dimensionValue is
   * also used to set the name of the metric
   *
   * @param dimensionName  name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param value          individual metric to be published for the dimension
   */
  public void put(String dimensionName, String dimensionValue, double value) {
    this.putMetricData(dimensionName, dimensionValue, dimensionValue, StandardUnit.NONE, value);
  }

  /**
   * Publishes a new value for a specific metric using the default unit (StandardUnit.NONE)
   *
   * @param dimensionName  name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName     the name of the metric - e.g <code>PAGES_VISITED</code>
   * @param value          individual metric to be published for the dimension
   */
  public void put(String dimensionName, String dimensionValue, String metricName, double value) {
    this.putMetricData(dimensionName, dimensionValue, metricName, StandardUnit.NONE, value);
  }

  /**
   * Publishes a new value that will be interpreted as milliseconds
   *
   * @param dimensionName  name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName     the name of the metric - e.g <code>MILLISECONDS_TO_LOAD</code>
   * @param value          individual metric to be published for the dimension
   */
  public void putMilliseconds(String dimensionName, String dimensionValue, String metricName, double value) {
    this.putMetricData(dimensionName, dimensionValue, metricName, StandardUnit.MILLISECONDS, value);
  }

  /**
   * Publishes a new value that will be interpreted as seconds
   *
   * @param dimensionName  name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName     the name of the metric - e.g <code>SECONDS_TO_LOAD</code>
   * @param value          individual metric to be published for the dimension
   */
  public void putSeconds(String dimensionName, String dimensionValue, String metricName, double value) {
    this.putMetricData(dimensionName, dimensionValue, metricName, StandardUnit.SECONDS, value);
  }

  /**
   * Publishes a new value that will be interpreted as count
   *
   * @param dimensionName  name for the dimension - e.g. <code>UNIQUE_PAGES</code>
   * @param dimensionValue a value for the dimension - e.g. <code>URLS</code>
   * @param metricName     the name of the metric - e.g <code>PAGES_VISITED</code>
   * @param value          individual metric to be published for the dimension
   */
  public void putCount(String dimensionName, String dimensionValue, String metricName, double value) {
    this.putMetricData(dimensionName, dimensionValue, metricName, StandardUnit.COUNT, value);
  }

  private void putMetricData(String dimensionName, String dimensionValue, String metricName, StandardUnit standardUnit, double value) {
    Dimension dimension = Dimension.builder().name(dimensionName).value(dimensionValue).build();
    MetricDatum datum = MetricDatum.builder().metricName(metricName).unit(standardUnit).value(value).dimensions(dimension).build();
    PutMetricDataRequest request = PutMetricDataRequest.builder().namespace(this.namespace).metricData(datum).build();

    client.putMetricData(request);
  }
}
