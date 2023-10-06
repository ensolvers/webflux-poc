package com.ensolvers.webfluxpoc.services;

import com.ensolvers.webfluxpoc.models.Film;
import com.ensolvers.webfluxpoc.repositories.FilmsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FilmsService {
  FilmsRepository repository;

  public FilmsService(FilmsRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Mono<Film> getById(Long id) {
    return this.repository.findById(id);
  }

  @Transactional(readOnly = true)
  public Flux<Film> getAll() {
    return this.repository.findAll();
  }
}
