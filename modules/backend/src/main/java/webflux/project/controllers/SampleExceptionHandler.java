package webflux.project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice
public class SampleExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(SampleExceptionHandler.class);

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNoSuchElementException(Exception e) {
    LOGGER.error(e.getMessage(), e);
    return new ResponseEntity<>("Object not found", HttpStatus.NOT_FOUND);
  }
}
