package webflux;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import utils.Env;

import java.util.Objects;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

public class WebFluxGetByIdSimulationWithSleep extends Simulation {
  Integer users = Integer.getInteger("users", 1);
  Integer rampUsersDuration = Objects.requireNonNull(Integer.getInteger("rampUsersDuration", null), "Environment variable required: rampUsersDuration (seconds)");
  Env env = Env.valueOf(Objects.requireNonNull(System.getProperty("env"), "Environment variable required: env").toUpperCase());
  Integer iterations = Integer.getInteger("iterations", 1);
  HttpProtocolBuilder httpProtocol = http
    .baseUrl(env.getAPIUrl())
    .acceptHeader("*/*")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");

  ScenarioBuilder scn = scenario("WebFluxGetTestWithSleep")
    .repeat(iterations)
    .on(
      exec(
        http("getByIdWithSleep")
          .get("/api/dynamic-properties/sleep/1"))
        .pause(1)
    );

  {
    setUp(scn.injectOpen(rampUsers(users).during(rampUsersDuration))).protocols(httpProtocol);
  }
}
