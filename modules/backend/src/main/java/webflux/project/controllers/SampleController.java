package webflux.project.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import webflux.project.models.Sample;
import webflux.project.services.SampleService;

@RestController
@RequestMapping("/sample")
public class SampleController {

  SampleService service;

  SampleController(SampleService service) {
    this.service = service;
  }

  @GetMapping
  public Mono<Page<Sample>> read(Pageable pageable) {
    return this.service.read(pageable);
  }

  @GetMapping("/{id}")
  public Mono<Sample> getById(@PathVariable("id") Long id) {
    return this.service.getById(id);
  }
}
