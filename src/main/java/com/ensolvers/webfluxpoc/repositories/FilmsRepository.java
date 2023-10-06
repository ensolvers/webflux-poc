package com.ensolvers.webfluxpoc.repositories;

import com.ensolvers.webfluxpoc.models.Film;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilmsRepository extends ReactiveCrudRepository<Film, Long> {
}
