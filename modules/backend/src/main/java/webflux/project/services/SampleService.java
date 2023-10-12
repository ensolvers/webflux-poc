package webflux.project.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import webflux.project.models.Sample;
import webflux.project.repositories.SampleRepository;

import java.util.NoSuchElementException;

@Service
public class SampleService {
  SampleRepository repository;

  SampleService(SampleRepository repository) {
    this.repository = repository;
  }

  public Mono<Sample> getById(Long id) {
    return this.repository.findById(id).switchIfEmpty(Mono.error(new NoSuchElementException("Sample with id " + id + " not found")));
  }

  public Mono<Page<Sample>> read(Pageable pageable) {
    return this.repository.findAllBy(pageable)
      .collectList()
      .zipWith(this.repository.count()) // Maybe there's a better way to do paging
      .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
  }
}
