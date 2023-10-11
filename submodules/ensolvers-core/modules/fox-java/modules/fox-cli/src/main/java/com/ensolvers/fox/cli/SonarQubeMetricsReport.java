package com.ensolvers.fox.cli;

import com.ensolvers.fox.alerts.SlackService;
import com.ensolvers.fox.quality.SonarQubeService;
import com.ensolvers.fox.quality.model.SonarQubeMetricHistoryResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;
import java.lang.System.Logger;
import java.util.stream.Collectors;
import picocli.CommandLine;

/**
 * Command-line application that allows to generate and send SonarQube reports to a Slack channel
 *
 * @author José Matías Rivero (jose.matias.rivero@gmail.com)
 */
@CommandLine.Command(name = "sonarqube-metrics-report")
public class SonarQubeMetricsReport implements Callable<Integer> {

  @CommandLine.Option(names = { "--sonar-token" }, description = "SonarQube token needed to interact with the API", required = true)
  private String sonarToken;

  @CommandLine.Option(names = { "--sonar-component" }, description = "SonarQube component from which the metrics will be fetched", required = true)
  private String sonarComponent;

  @CommandLine.Option(names = { "--slack-bot-token" }, description = "Slack bot token that will be used to publish the results", required = true)
  private String slackBotToken;

  @CommandLine.Option(names = { "--slack-channel" }, description = "Slack channel ID in which the results will be published", required = true)
  private String slackChannel;

  @Override
  public Integer call() throws Exception {
    SlackService slackService = new SlackService(slackBotToken, slackChannel);
    SonarQubeService sonarQubeService = new SonarQubeService(sonarToken);

    try {
      String metric = "coverage";

      SonarQubeMetricHistoryResponse history = sonarQubeService.getMetricHistory(this.sonarComponent, metric, Instant.now().minus(10, ChronoUnit.DAYS),
          Instant.now());

      String message;

      if (!history.getMeasures().isEmpty()) {
        message = "Measures for metric: " + metric + "\n";
        message = message + history.getMeasures().get(0).getHistory().stream().map(m -> m.getDate() + ": " + m.getValue()).collect(Collectors.joining("\n"));
      } else {
        message = "No measures for metric: " + metric;
      }

      slackService.sendMessageWithColor(message, "#CCCCCC");
    } catch (Exception e) {
      System.getLogger("SonarQubeMetricsReport").log(Logger.Level.ERROR, "Error when trying to execute SonarQube publishing%n " + e.getMessage());
    }

    return 1;
  }

  public static void main(String... args) {
    int exitCode = new CommandLine(new SonarQubeMetricsReport()).execute(args);
    System.exit(exitCode);
  }
}
