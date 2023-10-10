package com.ensolvers.webfluxpoc;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
@EnableR2dbcRepositories
public class WebfluxPocApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(WebfluxPocApplication.class).web(WebApplicationType.REACTIVE).run(args);
  }

}
