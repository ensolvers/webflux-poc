package com.ensolvers.webfluxpoc.services;

import com.ensolvers.webfluxpoc.models.Film;
import com.ensolvers.webfluxpoc.repositories.FilmsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FilmsService {
  FilmsRepository repository;

  public FilmsService(FilmsRepository repository) {
    this.repository = repository;
  }

  public Mono<Film> getById(Long id) {
    return this.repository
      .findById(id)
      .switchIfEmpty(Mono.error(new Exception("Film Not Found")));
  }

  public Flux<Film> getAll(Pageable pageable) {
    return this.repository.findAllBy(pageable);
  }
}
