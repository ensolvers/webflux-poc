package webflux.project.services;

import webflux.project.models.DynamicProperty;
import webflux.project.repositories.DynamicPropertiesRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DynamicPropertiesService {
  DynamicPropertiesRepository repository;

  public DynamicPropertiesService(DynamicPropertiesRepository repository) {
    this.repository = repository;
  }

  public Mono<DynamicProperty> getById(Long id) {
    return this.repository.findById(id).switchIfEmpty(Mono.error(new Exception("Dynamic Property Not Found")));
  }

}
