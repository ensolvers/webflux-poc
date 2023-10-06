package com.ensolvers.webfluxpoc.controllers;

import com.ensolvers.webfluxpoc.models.Film;
import com.ensolvers.webfluxpoc.services.FilmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/films")
public class FilmsController {
  private static Logger log = LoggerFactory.getLogger(FilmsController.class);
  FilmsService service;

  public FilmsController(FilmsService service) {
    this.service = service;
  }

  @GetMapping("/{id}")
  public Mono<Film> getById(@PathVariable(value = "id") Long id) {
    return this.service.getById(id);
  }

  @GetMapping
  public Flux<Film> getAll() {
    return this.service.getAll();
  }
}