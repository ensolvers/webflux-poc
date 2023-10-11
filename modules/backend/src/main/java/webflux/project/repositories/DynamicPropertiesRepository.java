package webflux.project.repositories;

import webflux.project.models.DynamicProperty;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicPropertiesRepository extends ReactiveSortingRepository<DynamicProperty, Long>, ReactiveCrudRepository<DynamicProperty, Long> {
}
