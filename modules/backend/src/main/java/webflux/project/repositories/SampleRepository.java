package webflux.project.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import webflux.project.models.Sample;

@Repository
public interface SampleRepository extends ReactiveSortingRepository<Sample, Long>, ReactiveCrudRepository<Sample, Long> {
  Flux<Sample> findAllBy(Pageable pageable);
}
