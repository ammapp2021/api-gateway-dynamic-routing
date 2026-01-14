package com.example.gateeway.controller;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin")
public class RouteController {

    private final RouteDefinitionRepository repository;
    private final ApplicationEventPublisher publisher;

    public RouteController(RouteDefinitionRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @PostMapping
    public Mono<String> add(@RequestBody RouteDefinition route) {
        return repository.save(Mono.just(route))
                .then(Mono.fromRunnable(() -> publisher.publishEvent(new RefreshRoutesEvent(this))))
                .thenReturn("Route Saved and Gateway Refreshed!");
    }

    @DeleteMapping("/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return repository.delete(Mono.just(id))
                .then(Mono.fromRunnable(() -> publisher.publishEvent(new RefreshRoutesEvent(this))))
                .thenReturn("Route Deleted and Gateway Refreshed!");
    }
}