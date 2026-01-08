package com.example.gateeway.config;

import com.example.gateeway.service.RouteService;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DynamicRouteRepository implements RouteDefinitionRepository {

    private final RouteService routeService;

    public DynamicRouteRepository(RouteService routeService) {
        this.routeService = routeService;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return routeService.getAllRoutes();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return routeService.saveRoute(route);
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeService.deleteRoute(routeId);
    }
}