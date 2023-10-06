package com.ensolvers.webfluxpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class})
@EnableR2dbcRepositories
public class WebfluxPocApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebfluxPocApplication.class, args);
  }

}
