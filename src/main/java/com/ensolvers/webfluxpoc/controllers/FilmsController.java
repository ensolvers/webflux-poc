package com.ensolvers.webfluxpoc.controllers;

import com.ensolvers.webfluxpoc.dtos.FilmDTO;
import com.ensolvers.webfluxpoc.services.FilmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
  public Mono<ResponseEntity<FilmDTO>> getById(@PathVariable(value = "id") Long id) {
    return this.service.getById(id)
      .map(film -> ResponseEntity.ok(FilmDTO.toDto(film)))
      .onErrorReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  @GetMapping
  public ResponseEntity<Flux<FilmDTO>> getAll() {
    return ResponseEntity.ok().body(this.service.getAll().map(FilmDTO::toDto));
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(Exception e) {
    log.error("Error: [%s]".formatted(e.getMessage()));
    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}