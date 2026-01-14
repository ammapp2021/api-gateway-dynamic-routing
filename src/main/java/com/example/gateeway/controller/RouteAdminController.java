/*package com.example.gateeway.controller;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin/routes")
public class RouteAdminController {
    private final RouteDefinitionRepository repository;
    private final ApplicationEventPublisher publisher;

    public RouteAdminController(RouteDefinitionRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @PostMapping
    public Mono<String> create(@RequestBody RouteDefinition route) {
        return repository.save(Mono.just(route))
                .then(Mono.fromRunnable(() -> publisher.publishEvent(new RefreshRoutesEvent(this))))
                .thenReturn("Route updated in Redis!");
    }

    @DeleteMapping("/{id}")
    public Mono<String> delete(@PathVariable String id) {
        return repository.delete(Mono.just(id))
                .then(Mono.fromRunnable(() -> publisher.publishEvent(new RefreshRoutesEvent(this))))
                .thenReturn("Route deleted!");
    }
}*/