package com.ensolvers.webfluxpoc.repositories;

import com.ensolvers.webfluxpoc.models.Film;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface FilmsRepository extends ReactiveSortingRepository<Film, Long>, ReactiveCrudRepository<Film, Long> {

  Flux<Film> findAllBy(Pageable pageable);

}
