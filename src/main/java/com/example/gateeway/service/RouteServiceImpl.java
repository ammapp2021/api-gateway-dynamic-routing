package com.example.gateeway.service;

import com.example.gateeway.model.RouteEntity;
import com.example.gateeway.repository.RouteRepository;
import com.example.gateeway.service.RouteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@RefreshScope
@Service
public class RouteServiceImpl implements RouteService {

    private final RouteRepository repository;
    private final ApplicationEventPublisher publisher;
    private final ObjectMapper mapper = new ObjectMapper();

    public RouteServiceImpl(RouteRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public Flux<RouteDefinition> getAllRoutes() {
        List<RouteDefinition> routes = repository.findByEnabledTrue()
                .stream()
                .map(this::convert)
                .toList();
        return Flux.fromIterable(routes);
    }

    private RouteDefinition convert(RouteEntity entity) {
        RouteDefinition def = new RouteDefinition();
        def.setId(entity.getId());
        def.setUri(URI.create(entity.getUri()));
        try {
            def.setPredicates(
                    mapper.readValue(entity.getPredicates(), new TypeReference<List<PredicateDefinition>>() {}));
            if (entity.getFilters() != null && !entity.getFilters().isBlank()) {
                def.setFilters(
                        mapper.readValue(entity.getFilters(), new TypeReference<List<FilterDefinition>>() {}));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    @Override
    public Mono<Void> saveRoute(Mono<RouteDefinition> routeMono) {
        return routeMono.flatMap(route -> {
            try {
                RouteEntity entity = new RouteEntity();
                entity.setId(route.getId());
                entity.setUri(route.getUri().toString());
                entity.setPredicates(mapper.writeValueAsString(route.getPredicates()));
                entity.setFilters(route.getFilters() != null ? mapper.writeValueAsString(route.getFilters()) : null);
                entity.setEnabled(true);
                repository.save(entity);
                publisher.publishEvent(new RefreshRoutesEvent(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> deleteRoute(Mono<String> routeId) {
        return routeId.flatMap(id -> {
            repository.deleteById(id);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.empty();
        });
    }
}
