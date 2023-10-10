package com.ensolvers.webfluxpoc.controllers;

import com.ensolvers.webfluxpoc.models.DynamicProperty;
import com.ensolvers.webfluxpoc.services.DynamicPropertiesService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/dynamic-properties")
public class DynamicPropertiesController {
  private static final Logger log = LoggerFactory.getLogger(DynamicPropertiesController.class);
  DynamicPropertiesService service;

  AsyncLoadingCache<Long, DynamicProperty> dynamicPropertyCache = Caffeine
    .newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .buildAsync((id, executor) -> this.service.getByIdWithoutCache(id).toFuture());

  public DynamicPropertiesController(DynamicPropertiesService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<DynamicProperty>> getByIdWithoutCache(@PathVariable("id") Long id) {
    return this.service
      .getByIdWithoutCache(id)
      .map(ResponseEntity::ok)
      .onErrorReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @GetMapping("/sleep/{id}")
  @Cacheable("dynamic-properties")
  public Mono<DynamicProperty> getByIdCachedWithSleep(@PathVariable("id") Long id) throws InterruptedException {
    Thread.sleep(100);
    return Mono.fromCompletionStage(this.dynamicPropertyCache.get(id));
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(Exception e) {
    log.error("Error: [%s]".formatted(e.getMessage()));
    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

}

