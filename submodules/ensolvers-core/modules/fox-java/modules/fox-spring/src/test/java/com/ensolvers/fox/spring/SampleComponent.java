package com.ensolvers.fox.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
public class SampleComponent {

  public String helloWorld() {
    return "Hey there";
  }

}
