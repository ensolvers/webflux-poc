package com.ensolvers.fox.quality;

import com.ensolvers.fox.quality.model.SonarQubeMetricHistoryResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.NoSuchElementException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A utils class that allows to interact with SonarQube (mostly for data fetching) via its API
 *
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
public class SonarQubeService {

  private static final String SONAR_API_BASE_PATH = "https://sonarcloud.io/api";
  private final ObjectMapper objectMapper;
  private final String token;

  private final OkHttpClient client;

  public SonarQubeService(String token) {
    this.token = token;
    this.client = new OkHttpClient().newBuilder().build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    this.objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
  }

  /**
   * Returns the historic values for a particular metric taken from a specific component within a specific time range
   *
   * @param component key of the component from which the values must be fetched
   * @param metric    specific metric from which we want to get the historic values
   * @param from      starting date from which measures will be filtered
   * @param to        end date from which measures will be filtered
   * @return a {@code SonarQubeMetricHistoryResponse} structure with all the individual measures
   * @throws IOException if an error occurs during the fetching
   */
  public SonarQubeMetricHistoryResponse getMetricHistory(String component, String metric, Instant from, Instant to) throws IOException, NoSuchElementException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("UTC"));

    String tokenInBase64 = Base64.getEncoder().withoutPadding().encodeToString((this.token + ":").getBytes(StandardCharsets.UTF_8));

    Request request = new Request.Builder().url(SONAR_API_BASE_PATH + "/measures/search_history?" + "from=" + formatter.format(from) + "&to="
        + formatter.format(to) + "&component=" + component + "&metrics=" + metric + "&ps=1000").method("GET", null)
        .addHeader("Authorization", "Basic " + tokenInBase64).build();
    Response response = client.newCall(request).execute();

    if (response.code() == 404) {
      throw new NoSuchElementException("Platform or component not found");
    }

    String stringResponse = response.body().string();
    return this.objectMapper.readValue(stringResponse, SonarQubeMetricHistoryResponse.class);
  }
}
