package com.ensolvers.webfluxpoc.services;

import com.ensolvers.webfluxpoc.models.DynamicProperty;
import com.ensolvers.webfluxpoc.repositories.DynamicPropertiesRepository;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
public class DynamicPropertiesService {
  DynamicPropertiesRepository repository;
  AsyncLoadingCache<Long, DynamicProperty> dynamicPropertyCache = Caffeine
    .newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .buildAsync((id, executor) -> this.repository.findById(id).toFuture());

  public DynamicPropertiesService(DynamicPropertiesRepository repository) {
    this.repository = repository;
  }

  @Cacheable("dynamic_properties")
  public Mono<DynamicProperty> getById(Long id) {
    return Mono
      .fromCompletionStage(this.dynamicPropertyCache.get(id))
      .switchIfEmpty(Mono.error(new Exception("Dynamic Property Not Found")));
  }
}
