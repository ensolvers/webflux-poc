package webflux.project.controllers;

import com.ensolvers.core.common.model.DynamicProperty;
import webflux.project.services.DynamicPropertiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dynamic-properties")
public class DynamicPropertiesController {
  private static final Logger log = LoggerFactory.getLogger(DynamicPropertiesController.class);
  DynamicPropertiesService service;

  public DynamicPropertiesController(DynamicPropertiesService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<DynamicProperty>> getById(@PathVariable("id") Long id) {
    return this.service.getById(id).map(ResponseEntity::ok).onErrorReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(Exception e) {
    log.error("Error: [" + e.getMessage() + "]");
    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
