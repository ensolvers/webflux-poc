package com.ensolvers.webfluxpoc.controllers;

import com.ensolvers.webfluxpoc.dtos.FilmDTO;
import com.ensolvers.webfluxpoc.models.Film;
import com.ensolvers.webfluxpoc.repositories.FilmsRepository;
import com.ensolvers.webfluxpoc.services.FilmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping(value = "/api/films")
public class FilmsController {
  private static final Logger log = LoggerFactory.getLogger(FilmsController.class);
  FilmsService service;
  FilmsRepository repository;

  public FilmsController(FilmsService service, FilmsRepository repository) {
    this.service = service;
    this.repository = repository;
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<FilmDTO>> getById(@PathVariable(value = "id") Long id) {
    return this.service.getById(id)
      .map(film -> ResponseEntity.ok(FilmDTO.toDto(film)))
      .onErrorReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }

  // Spring automatically wraps Flux inside a Response Entity
  @GetMapping
  public Mono<Page<FilmDTO>> getAll(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "50") Integer size) {
    Pageable pageable = PageRequest.of(page, size);
    return this.service.getAll(pageable)
      .map(FilmDTO::toDto)
      .collectList()
      .zipWith(this.repository.count())
      .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
  }

  @ExceptionHandler
  public ResponseEntity<String> handle(Exception e) {
    log.error("Error: [%s]".formatted(e.getMessage()));
    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}