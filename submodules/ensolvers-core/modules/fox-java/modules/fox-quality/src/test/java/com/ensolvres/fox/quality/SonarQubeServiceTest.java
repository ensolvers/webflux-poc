package com.ensolvres.fox.quality;

import static org.junit.jupiter.api.Assertions.*;

import com.ensolvers.fox.quality.SonarQubeService;
import com.ensolvers.fox.quality.model.SonarQubeMetricHistoryResponse;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class SonarQubeServiceTest {

  private SonarQubeService service;

  @BeforeEach
  private void createService() {
    // TODO replace to an env var and provide via an action/pipeline
    this.service = new SonarQubeService("token");
  }

  @Test
  @Disabled("")
  void getMetric() throws Exception {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    Instant from = format.parse("2021-07-01").toInstant();
    Instant to = format.parse("2021-07-30").toInstant();

    // TODO replace component to an env var and provide via an action/pipeline

    SonarQubeMetricHistoryResponse coverage = this.service.getMetricHistory("component", "coverage", from, to);

    // check that at least one historic coverage datapoint can be fetched
    assertFalse(coverage.getMeasures().isEmpty());
    assertEquals("coverage", coverage.getMeasures().get(0).getMetric());
    assertFalse(coverage.getMeasures().get(0).getHistory().isEmpty());
    assertNotNull(coverage.getMeasures().get(0).getHistory().get(0).getDate());
    assertNotNull(coverage.getMeasures().get(0).getHistory().get(0).getValue());
    assertFalse(coverage.getMeasures().get(0).getHistory().get(0).getValue().isEmpty());
  }

  @Test
  void isoDateParsing() throws Exception {
    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse("2021-07-19T01:06:13+0200");

    assertEquals("Sun", date.toString().substring(0, 3));
  }
}
