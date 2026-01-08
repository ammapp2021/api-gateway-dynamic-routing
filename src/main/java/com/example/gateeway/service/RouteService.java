package com.example.gateeway.service;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface RouteService {

    Flux<RouteDefinition> getAllRoutes();

    Mono<Void> saveRoute(Mono<RouteDefinition> routeMono);

    Mono<Void> deleteRoute(Mono<String> routeId);
}
